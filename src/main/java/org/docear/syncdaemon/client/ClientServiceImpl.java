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

	// @Override
	// @Deprecated
	// public Future<User> login(final String username, final String password) {
	// final WebResource loginResource =
	// restClient.resource(serviceUrl).path("user/login");
	// MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("username", username);
	// formData.add("password", password);
	// final ClientResponse loginResponse =
	// loginResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class,
	// formData);
	//
	// if (loginResponse.getStatus() == 200) {
	// final User user = new User(username,
	// loginResponse.getEntity(String.class));
	// return Futures.successful(user);
	// } else {
	// return null;
	// }
	// }
	//
	// @Override
	// public Future<Boolean> listenIfUpdatesOccur(final User user, final
	// MapIdentifier mapIdentifier) {
	// return Futures.future(new Callable<Boolean>() {
	//
	// @Override
	// public Boolean call() throws Exception {
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/listen");
	//
	// final ClientResponse loginResponse = resource.get(ClientResponse.class);
	// return loginResponse.getStatus() == 200;
	// }
	// }, clientController.system().dispatcher());
	//
	// }
	//
	// @Override
	// public Future<JsonNode> getMapAsXml(final User user, final MapIdentifier
	// mapIdentifier) {
	//
	// try {
	// final WebResource mapAsXmlResource =
	// preparedResource(user).path("project/" + mapIdentifier.getProjectId() +
	// "/map/" + mapIdentifier.getMapId() + "/xml");
	// final JsonNode response = new
	// ObjectMapper().readTree(mapAsXmlResource.get(String.class));
	// return Futures.successful(response);
	// } catch (Exception e) {
	// e.printStackTrace();
	// return Futures.failed(e);
	// }
	//
	// }
	//
	// @Override
	// public Future<GetUpdatesResponse> getUpdatesSinceRevision(final User
	// user, final MapIdentifier mapIdentifier, final int sinceRevision) {
	//
	// int currentRevision = -1;
	// List<MapUpdate> updates = new ArrayList<MapUpdate>();
	// final WebResource fetchUpdates = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/updates/" + sinceRevision);
	// final ClientResponse response = fetchUpdates.get(ClientResponse.class);
	// final ObjectMapper mapper = new ObjectMapper();
	// try {
	// JsonNode json = mapper.readTree(response.getEntity(String.class));
	// currentRevision = json.get("currentRevision").asInt();
	//
	// Iterator<JsonNode> it = json.get("orderedUpdates").iterator();
	// while (it.hasNext()) {
	// final JsonNode mapUpdateJson = it.next();
	//
	// final MapUpdate.Type type =
	// MapUpdate.Type.valueOf(mapUpdateJson.get("type").asText());
	// switch (type) {
	// case AddNode:
	// updates.add(mapper.treeToValue(mapUpdateJson, AddNodeUpdate.class));
	// break;
	// case ChangeNodeAttribute:
	// updates.add(mapper.treeToValue(mapUpdateJson,
	// ChangeNodeAttributeUpdate.class));
	// break;
	// case DeleteNode:
	// updates.add(mapper.treeToValue(mapUpdateJson, DeleteNodeUpdate.class));
	// break;
	// case MoveNode:
	// updates.add(mapper.treeToValue(mapUpdateJson, MoveNodeUpdate.class));
	// break;
	// case ChangeEdgeAttribute:
	// updates.add(mapper.treeToValue(mapUpdateJson,
	// ChangeEdgeAttributeUpdate.class));
	// break;
	//
	// }
	//
	// }
	// } catch (Exception e) {
	// return Futures.failed(e);
	// }
	// return Futures.successful(new GetUpdatesResponse(currentRevision,
	// updates));
	//
	// }
	//
	// @Override
	// public Future<String> createNode(final User user, final MapIdentifier
	// mapIdentifier, final String parentNodeId) {
	//
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/node/create");
	// final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("parentNodeId", parentNodeId);
	//
	// final ClientResponse response = resource.post(ClientResponse.class,
	// formData);
	// try {
	// final AddNodeUpdate update = new
	// ObjectMapper().readValue(response.getEntity(String.class),
	// AddNodeUpdate.class);
	// return Futures.successful(update.getNewNodeId());
	// } catch (Exception e) {
	// e.printStackTrace();
	// return Futures.failed(e);
	// }
	// }
	//
	// @Override
	// public Future<Boolean> moveNodeTo(final User user, final MapIdentifier
	// mapIdentifier, final String newParentId, final String nodeToMoveId, final
	// int newIndex) {
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/node/move");
	// final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("newParentNodeId", newParentId);
	// formData.add("nodetoMoveId", nodeToMoveId);
	// formData.add("newIndex", newIndex + "");
	//
	// final ClientResponse response = resource.post(ClientResponse.class,
	// formData);
	// LogUtils.info("Status: " + response.getStatus());
	// return Futures.successful(response.getStatus() == 200);
	//
	// }
	//
	// @Override
	// public Future<Boolean> removeNode(final User user, final MapIdentifier
	// mapIdentifier, final String nodeId) {
	//
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/node/delete");
	// final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("nodeId", nodeId);
	//
	// ClientResponse response = resource.delete(ClientResponse.class,
	// formData);
	//
	// LogUtils.info("Status: " + response.getStatus());
	// return Futures.successful(response.getStatus() == 200);
	//
	// }
	//
	// @Override
	// public Future<Boolean> changeNode(final User user, final MapIdentifier
	// mapIdentifier, final String nodeId, final String attribute, final Object
	// value) {
	// try {
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/node/change");
	// final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("nodeId", nodeId);
	// formData.add(attribute, value == null ? null : value.toString());
	//
	// LogUtils.info("locking node");
	// // boolean isLocked =
	// boolean isLocked = Await.result(lockNode(user, mapIdentifier, nodeId),
	// Duration.create("5 seconds"));
	// if (!isLocked)
	// return Futures.successful(false);
	// LogUtils.info("changing");
	// ClientResponse response = resource.post(ClientResponse.class, formData);
	// LogUtils.info("releasing node");
	// releaseNode(user, mapIdentifier, nodeId);
	//
	// LogUtils.info("Status: " + response.getStatus());
	// return Futures.successful(response.getStatus() == 200);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// return Futures.failed(e);
	// }
	// }
	//
	// @Override
	// public Future<Boolean> changeEdge(final User user, final MapIdentifier
	// mapIdentifier, String nodeId, String attribute, Object value) {
	// try {
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/node/changeEdge");
	// final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("nodeId", nodeId);
	// formData.add(attribute, value.toString());
	//
	// LogUtils.info("changing");
	// ClientResponse response = resource.post(ClientResponse.class, formData);
	//
	// LogUtils.info("Status: " + response.getStatus());
	// return Futures.successful(response.getStatus() == 200);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// return Futures.failed(e);
	// }
	// }
	//
	// private Future<Boolean> lockNode(final User user, final MapIdentifier
	// mapIdentifier, final String nodeId) {
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/node/requestLock");
	// final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("nodeId", nodeId);
	//
	// ClientResponse response = resource.post(ClientResponse.class, formData);
	// LogUtils.info("Status: " + response.getStatus());
	// return Futures.successful(response.getStatus() == 200);
	// }
	//
	// private Future<Boolean> releaseNode(final User user, final MapIdentifier
	// mapIdentifier, final String nodeId) {
	// final WebResource resource = preparedResource(user).path("project/" +
	// mapIdentifier.getProjectId() + "/map/" + mapIdentifier.getMapId() +
	// "/node/releaseLock");
	// final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
	// formData.add("nodeId", nodeId);
	//
	// ClientResponse response = resource.post(ClientResponse.class, formData);
	//
	// LogUtils.info("Status: " + response.getStatus());
	// return Futures.successful(response.getStatus() == 200);
	// }

	// ClientService implementation
	@Override
	public UploadResponse upload(Project project, FileMetaData fileMetaData) throws FileNotFoundException {
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
			final WebResource request = preparedResource(fileMetaData.getProjectId()).path("file").path(urlEncodedFilePath).queryParam("parentRev", "" + fileMetaData.getRevision())
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
				final FileMetaData currentOriMetaData = getCurrentFileMetaData(fileMetaData);
				return new UploadResponse(currentOriMetaData, newFileMeta);
			}

		} finally {
			IOUtils.closeQuietly(fileStream);
		}
	}

	@Override
	public void download(FileMetaData currentServerMetaData) {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public ProjectResponse getProjects(User user) {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public FolderMetaData getFolderMetaData(FileMetaData folderMetaData) {
		final String projectId = folderMetaData.getProjectId();

		final String urlEncodedFilePath = normalizePath(folderMetaData.getPath());
		// create request
		final WebResource request = preparedResource(projectId).path("metadata").path(urlEncodedFilePath);

		ClientResponse response = request.get(ClientResponse.class);

		// on success
		if (response.getStatus() == 200)
			return serverMetadataToLocalFolderMetaData(projectId, response.getEntity(String.class));
		else
			return null;
	}

	@Override
	public FileMetaData getCurrentFileMetaData(FileMetaData fileMetaData) {
		final String projectId = fileMetaData.getProjectId();

		final String urlEncodedFilePath = normalizePath(fileMetaData.getPath());
		// create request
		final WebResource request = preparedResource(fileMetaData.getProjectId()).path("metadata").path(urlEncodedFilePath);

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

	private WebResource preparedResource(String projectId) {
		return restClient.resource(serviceUrl).path("project").path(projectId) // path
				// authentication
				.queryParam("username", username()).queryParam("accessToken", accessToken());
	}

	private String username() {
		return config.getString("daemon.client.user.name");
	}

	private String accessToken() {
		return config.getString("daemon.client.user.token");
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
}