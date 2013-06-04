package org.docear.syncdaemon.client;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.fest.assertions.Assertions;
import org.junit.Ignore;
import org.junit.Test;

public class GetFileMetaDataTest {

	@Test
	@Ignore
	public void testGetFileMetaData() {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final FileMetaData metadata = clientService.getCurrentFileMetaData(new FileMetaData("507f191e810c19729de860ea", "/README.md", false));
		Assertions.assertThat(metadata.getProjectId()).isEqualTo("507f191e810c19729de860ea");
		Assertions.assertThat(metadata.getHash()).isEqualTo("122233a");
		Assertions.assertThat(metadata.getPath()).isEqualTo("/README.md");
		Assertions.assertThat(metadata.getRevision()).isEqualTo(1);
	}
	
	@Test
	@Ignore
	public void testFileNotPresent() {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final FileMetaData metadata = clientService.getCurrentFileMetaData(new FileMetaData("507f191e810c19729de860ea", "/NOT PRESENT", false));
		Assertions.assertThat(metadata).isNull();
	}
}
