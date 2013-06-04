package org.docear.syncdaemon.client;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.fest.assertions.Assertions;
import org.junit.Ignore;
import org.junit.Test;

public class GetFolderMetaDataTest {

	@Test
	@Ignore
	public void testGetFolderMetaData() {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final FolderMetaData folderMeta = clientService.getFolderMetaData(new FileMetaData("507f191e810c19729de860ea", "/", false));
		final FileMetaData rootMeta = folderMeta.getMetaData();
		Assertions.assertThat(rootMeta.getProjectId()).isEqualTo("507f191e810c19729de860ea");
		Assertions.assertThat(rootMeta.getHash()).isNull();
		Assertions.assertThat(rootMeta.getPath()).isEqualTo("/");
		Assertions.assertThat(rootMeta.getRevision()).isEqualTo(0);
	}
	
	@Test
	@Ignore
	public void testFolderNotPresent() {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final FolderMetaData metadata = clientService.getFolderMetaData(new FileMetaData("507f191e810c19729de860ea", "/NOT PRESENT", false));
		Assertions.assertThat(metadata).isNull();
	}
}
