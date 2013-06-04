package org.docear.syncdaemon.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.docear.syncdaemon.NeedsConfig;
import org.docear.syncdaemon.client.exceptions.NoFolderException;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.typesafe.config.Config;

public class ClientServiceImpl implements ClientService, NeedsConfig {
	private Config config;

	private String serviceUrl;
	private Client restClient;

	private void intialize() {
		/**
		 * important! WS does not run properly without the logging filter. Why?
		 * No Idea...
		 */
		final PrintStream stream = new PrintStream(new NullOutputStream());
		restClient = ApacheHttpClient.create();
		restClient.addFilter(new LoggingFilter(stream));

		// generate service url
		serviceUrl = config.getString("daemon.client.baseurl") + "/" + config.getString("daemon.client.api.version");
	}

	// ClientService implementation
	@Override
	public UploadResponse upload(User user, Project project, FileMetaData fileMetaData) throws FileNotFoundException {
		final String projectId = fileMetaData.getProjectId();
		// validation
		if (!project.getId().equals(projectId)) {
			throw new RuntimeException("file does not belong to project");
		}

		InputStream fileStream = null;
		try {
			// get file stream
			final String absoluteFilePath = project.getRootPath() + fileMetaData.getPath();
			final String urlEncodedFilePath = normalizePath(fileMetaData.getPath());
			InputStream fileInStream = new FileInputStream(absoluteFilePath);

			// create request
			final WebResource request = preparedResource(fileMetaData.getProjectId(), user).path("file").path(urlEncodedFilePath).queryParam("parentRev", "" + fileMetaData.getRevision())
					.queryParam("isZip", "false");

			ClientResponse response = request.type(MediaType.APPLICATION_OCTET_STREAM).put(ClientResponse.class, fileInStream);

			final FileMetaData newFileMeta = serverMetadataToLocalFileMetaData(projectId, response.getEntity(String.class));
			// check that file is no conflicted copy
			if (newFileMeta.getPath().equals(fileMetaData.getPath())) {
				// equal path => no conflict
				return new UploadResponse(newFileMeta);
			} else {
				// conflict!
				// get additional fileMetaData for original file
				final FileMetaData currentOriMetaData = getCurrentFileMetaData(user, fileMetaData);
				return new UploadResponse(currentOriMetaData, newFileMeta);
			}

		} finally {
			IOUtils.closeQuietly(fileStream);
		}
	}

	@Override
	public void download(User user, FileMetaData currentServerMetaData) {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public ProjectResponse getProjects(User user) {
		// create request
		final WebResource request = preparedResource(user).path("projects");

		ClientResponse response = request.get(ClientResponse.class);

		// on success
		if (response.getStatus() == 200)
			return new ProjectResponse(serverProjectListToLocalProject(response.getEntity(String.class)));
		else
			return null;
	}

	@Override
	public FolderMetaData getFolderMetaData(User user, FileMetaData folderMetaData) {
		final String projectId = folderMetaData.getProjectId();

		final String urlEncodedFilePath = normalizePath(folderMetaData.getPath());
		// create request
		final WebResource request = preparedResource(projectId, user).path("metadata").path(urlEncodedFilePath);

		ClientResponse response = request.get(ClientResponse.class);

		// on success
		if (response.getStatus() == 200)
			return serverMetadataToLocalFolderMetaData(projectId, response.getEntity(String.class));
		else
			return null;
	}

	@Override
	public FileMetaData getCurrentFileMetaData(User user, FileMetaData fileMetaData) {
		final String projectId = fileMetaData.getProjectId();

		final String urlEncodedFilePath = normalizePath(fileMetaData.getPath());
		// create request
		final WebResource request = preparedResource(fileMetaData.getProjectId(), user).path("metadata").path(urlEncodedFilePath);

		ClientResponse response = request.get(ClientResponse.class);

		// on success
		if (response.getStatus() == 200)
			return serverMetadataToLocalFileMetaData(projectId, response.getEntity(String.class));
		else
			return null;

	}

	// Needs Config implementation
	@Override
	public void setConfig(Config config) {
		this.config = config;
		this.intialize();
	}

	// private methods

	private WebResource preparedResource(User user) {
		return restClient.resource(serviceUrl).path("user") // path
				// authentication
				.queryParam("username", user.getUsername()).queryParam("accessToken", user.getAccessToken());
	}

	private WebResource preparedResource(String projectId, User user) {
		return restClient.resource(serviceUrl).path("project").path(projectId) // path
				// authentication
				.queryParam("username", user.getUsername()).queryParam("accessToken", user.getAccessToken());
	}

	/**
	 * converts "\" to "/" and encodes to UTF-8
	 */
	private String normalizePath(String path) {
		try {
			return URLEncoder.encode(path.replace("\\", "/"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Problem with UTF-8 Encoding");
		}
	}

	private FolderMetaData serverMetadataToLocalFolderMetaData(String projectId, String metadata) {
		try {
			final JsonNode metaJson = new ObjectMapper().readTree(metadata);
			final boolean dir = metaJson.get("dir").booleanValue();
			if (!dir) {
				throw new NoFolderException("resource is no folder");
			}
			final FileMetaData fileMetaData = serverMetadataToLocalFileMetaData(projectId, metadata);
			final List<FileMetaData> childrenData = new ArrayList<FileMetaData>();
			for (JsonNode metaNode : metaJson.get("contents")) {
				childrenData.add(serverMetadataToLocalFileMetaData(projectId, metaNode.toString()));
			}
			return new FolderMetaData(fileMetaData, childrenData);
		} catch (JsonMappingException e) {
			throw new RuntimeException("Invalid server metadata object.", e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Invalid server metadata object.", e);
		} catch (IOException e) {
			throw new RuntimeException("Invalid server metadata object.", e);
		}
	}

	private FileMetaData serverMetadataToLocalFileMetaData(String projectId, String metadata) {
		try {

			final JsonNode metaJson = new ObjectMapper().readTree(metadata);
			final String path = metaJson.get("path").textValue();
			final Long revision = metaJson.get("revision").longValue();
			final boolean dir = metaJson.get("dir").booleanValue();
			final boolean deleted = metaJson.get("deleted").booleanValue();
			// final Long bytes = metaJson.get("bytes").longValue();
			final String hash = metaJson.get("hash").textValue();
			return new FileMetaData(path, hash, projectId, dir, deleted, revision);
		} catch (Exception e) {
			throw new RuntimeException("Invalid server metadata object.", e);
		}
	}

	private List<Project> serverProjectListToLocalProject(String project) {
		try {
			final List<Project> projects = new ArrayList<Project>();
			for (final JsonNode projectJson : new ObjectMapper().readTree(project)) {
				final String id = projectJson.get("id").textValue();
				// final String name = metaJson.get("name").textValue();
				final Long revision = projectJson.get("revision").longValue();
				final List<String> user = new ArrayList<String>();
				for (JsonNode node : projectJson.get("authorizedUsers")) {
					user.add(node.toString());
				}

				projects.add(new Project(id, "", revision));
			}
			return projects;
		} catch (Exception e) {
			throw new RuntimeException("Invalid server metadata object.", e);
		}
	}
}