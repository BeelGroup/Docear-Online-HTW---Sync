package org.docear.syncdaemon.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.Ignore;
import org.junit.Test;

public class DownloadFileTest {
	private static final User user = new User("Julius", "Julius-token");

	@Test
	@Ignore
	public void testGetFile() throws IOException {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final FileMetaData fileMetaData = new FileMetaData("507f191e810c19729de860ea", "/README.md", false);
		final InputStream inStream = clientService.download(user, fileMetaData);
		final String fileContent = IOUtils.toString(inStream);
		IOUtils.closeQuietly(inStream);
		System.out.println(fileContent);
	}

	@Test
	@Ignore
	public void testNoFile() {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final ProjectResponse projectResponse = clientService.getProjects(new User("invalid", "invalid1215"));
		Assertions.assertThat(projectResponse).isNull();
	}
}
