package org.docear.syncdaemon.client;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.users.User;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;


public class GetFileMetaDataITest {
    private static final User user = new User("Julius", "Julius-token");

    @Test
    public void testGetFileMetaData() {
        final Daemon daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        final ClientService clientService = daemon.service(ClientService.class);
        
        final FileMetaData metadata = clientService.getCurrentFileMetaData(user, FileMetaData.file(File.separator+"README.md", "122233a", "507f191e810c19729de860ea", false, 0));
        assertThat(metadata.getProjectId()).isEqualTo("507f191e810c19729de860ea");
        assertThat(metadata.getHash()).isEqualTo("122233a");
        assertThat(metadata.getPath()).isEqualTo("/README.md");
        assertThat(metadata.getRevision()).isEqualTo(1);
    }

    @Test
    public void testFileNotPresent() {
        final Daemon daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        final ClientService clientService = daemon.service(ClientService.class);
        final FileMetaData metadata = clientService.getCurrentFileMetaData(user, FileMetaData.file(File.separator+"NOT PRESENT", "122233a", "507f191e810c19729de860ea", false, 0));
        assertThat(metadata).isNull();
    }
}
