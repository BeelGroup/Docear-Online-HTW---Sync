package org.docear.syncdaemon.client;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.users.User;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;


public class GetFileMetaDataTest {
    private static final User user = new User("Julius", "Julius-token");

    @Test
    @Ignore
    public void testGetFileMetaData() {
        final Daemon daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        final ClientService clientService = daemon.service(ClientService.class);
        final FileMetaData metadata = clientService.getCurrentFileMetaData(user, new FileMetaData("507f191e810c19729de860ea", File.separator+"README.md", false));
        assertThat(metadata.getProjectId()).isEqualTo("507f191e810c19729de860ea");
        assertThat(metadata.getHash()).isEqualTo("122233a");
        assertThat(metadata.getPath()).isEqualTo(File.separator+"README.md");
        assertThat(metadata.getRevision()).isEqualTo(1);
    }

    @Test
    @Ignore
    public void testFileNotPresent() {
        final Daemon daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        final ClientService clientService = daemon.service(ClientService.class);
        final FileMetaData metadata = clientService.getCurrentFileMetaData(user, new FileMetaData("507f191e810c19729de860ea", File.separator+"NOT PRESENT", false));
        assertThat(metadata).isNull();
    }
}
