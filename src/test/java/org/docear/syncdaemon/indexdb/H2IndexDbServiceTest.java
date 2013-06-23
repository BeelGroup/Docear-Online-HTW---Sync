package org.docear.syncdaemon.indexdb;

import static org.docear.syncdaemon.TestUtils.testDaemon;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    
    @Test(expected = PersistenceException.class)
    public void testAddDeleteProject() throws Exception {
        final String projectId = "projectId";
        service.setProjectRevision(projectId, 2);
        assertThat(service.getProjectRevision(projectId)).isEqualTo(2);
        service.deleteProject(projectId);
        assertThat(service.getProjectRevision(projectId)).isEqualTo(0);
    }

    @Test
    public void testGetFileMetaDatas() throws Exception {
        final String projectId = "hello";
        final String path1 = "/src/main/java/Main.java";
        final FileMetaData metaData1 = FileMetaData.newFile(path1, "6d32f9ecafba3ecfdf686471a4d1fec68bb99aa4c9a55a3e200df0f62c03c425e34446c5f8afb4625784e5a82ea333b01b13524f9313c30385b6d49d665028b3", projectId);
        final String path2 = "/src/test/java/MainTest.java";
        final FileMetaData metaData2 = FileMetaData.newFile(path2, "7d32f9ecafba3ecfdf686471a4d1fec68bb99aa4c9a55a3e200df0f62c03c425e34446c5f8afb4625784e5a82ea333b01b13524f9313c30385b6d49d665028b3", projectId);
        service.save(metaData1);
        service.save(metaData2);
        final Collection<String> savedPaths = Collections2.transform(service.getFileMetaDatas(projectId), new Function<FileMetaData, String>() {
            @Override
            public String apply(FileMetaData input) {
                return input.getPath();
            }
        });
        service.getFileMetaDatas(projectId);
        assertThat(savedPaths).hasSize(2);
        assertThat(savedPaths).contains(path1, path2);
    }

    @Test
    public void testGetProjects() throws Exception {
        final String project1Id = "project1";
        final int project1Rev = 1;
        service.setProjectRevision(project1Id, project1Rev);
        final String project2Id = "project2";
        final int project2Rev = 2;
        service.setProjectRevision(project2Id, project2Rev);
        assertThat(service.getProjectRevision(project1Id));


        final Map<String,Long> projectIdToRevisionMap = service.getProjects();
        assertThat(projectIdToRevisionMap).hasSize(2);
        assertThat(projectIdToRevisionMap.get(project1Id)).isEqualTo(project1Rev);
        assertThat(projectIdToRevisionMap.get(project2Id)).isEqualTo(project2Rev);
        assertThat(projectIdToRevisionMap.get("not there")).isNull();
    }
}
