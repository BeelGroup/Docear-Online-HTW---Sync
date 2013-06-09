package org.docear.syncdaemon.indexdb;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.docear.syncdaemon.TestUtils.testDaemon;
import static org.fest.assertions.Assertions.assertThat;

public class H2IndexDbServiceTest {
    IndexDbService service;
    private Daemon daemon;

    @Before
    public void setUp() throws Exception {
        daemon = testDaemon();
        daemon.onStart();
        service = daemon.service(IndexDbService.class);
    }

    @After
    public void tearDown() throws Exception {
        daemon.onStop();
        daemon = null;
        service = null;
    }

    @Test
    public void testSaveAsInsert() throws Exception {
        final FileMetaData metaData = newDemoFile();
        assertThat(service.getFileMetaData(metaData)).overridingErrorMessage("project does not exist => file meta data is null").isNull();
        service.save(metaData);
        final FileMetaData storedData = service.getFileMetaData(metaData);
        assertThat(storedData.getProjectId()).isEqualTo(metaData.getProjectId());
        assertThat(storedData.getPath()).isEqualTo(metaData.getPath());
        assertThat(storedData.getHash()).isEqualTo(metaData.getHash());
        assertThat(storedData.getRevision()).isEqualTo(0);
        assertThat(storedData.isDeleted()).isEqualTo(false);
        assertThat(storedData.isFolder()).isEqualTo(false);
    }

    private FileMetaData newDemoFile() {
        final String hash = "6d32f9ecafba3ecfdf686471a4d1fec68bb99aa4c9a55a3e200df0f62c03c425e34446c5f8afb4625784e5a82ea333b01b13524f9313c30385b6d49d665028b3";
        final String projectId = UUID.randomUUID().toString();
        return FileMetaData.newFile("/src/main/java/Main.java", hash, projectId);
    }

    @Test
    public void testSaveAsUpdate() throws Exception {
        final FileMetaData previousVersion = newDemoFile();
        service.save(previousVersion);
        assertThat(service.getFileMetaData(previousVersion)).isNotNull();
        final int newRevision = 10;
        final boolean isFolder = true;
        final FileMetaData newVersion = new FileMetaData(previousVersion.getPath(), null, previousVersion.getProjectId(), isFolder, false, newRevision);
        service.save(newVersion);
        final FileMetaData storedVersion = service.getFileMetaData(previousVersion);
        assertThat(storedVersion.getRevision()).isEqualTo(newRevision);
        assertThat(storedVersion.isFolder()).isEqualTo(isFolder);
    }


    @Test
    public void testProjectRevision() throws Exception {
        final String projectId = "earth";
        final long revision = 42L;
        service.setProjectRevision(projectId, revision);
        assertThat(service.getProjectRevision(projectId)).isEqualTo(revision);
    }
}
