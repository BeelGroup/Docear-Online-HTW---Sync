package org.docear.syncdaemon.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.fest.assertions.Assertions.assertThat;

public class ClientServiceITest {

    private static final User user = new User("Julius", "Julius-token");
    private static Project project;
    private static Daemon daemon;
    private static ClientService clientService;
    private static FileMetaData fileMetaData;

    @BeforeClass
    public static void setUpClass() throws FileNotFoundException {
        daemon = TestUtils.testDaemon();
        clientService = daemon.service(ClientService.class);

        String projectId = null;
        //get projectId of freeplane project
        for (Project project : clientService.getProjects(user).getProjects()) {
            if (project.getName().equals("Freeplane")) {
                projectId = project.getId();
            }
        }

        Assertions.assertThat(projectId).isNotNull();

        // get root for project
        String pathOfClass = ClientServiceITest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";

        project = new Project(projectId, rootPath, 8, "Freeplane");

        final String filename = "/rootFile.pptx";
        fileMetaData = new FileMetaData(filename, "", projectId, false, false, 0);
    }

    @AfterClass
    public static void tearDownClass() {
        final FileMetaData currentServerMeta = clientService.getCurrentFileMetaData(user, fileMetaData);
        if (!currentServerMeta.isDeleted())
            clientService.delete(user, project, FileMetaData.file(fileMetaData.getPath(), fileMetaData.getHash(), fileMetaData.getProjectId(), false, currentServerMeta.getRevision()));
    }

    @Before
    public void setUp() throws FileNotFoundException {
        final FileMetaData currentServerMeta = clientService.getCurrentFileMetaData(user, fileMetaData);
        if (currentServerMeta.isDeleted())
            clientService.upload(user, project, FileMetaData.file(fileMetaData.getPath(), fileMetaData.getHash(), fileMetaData.getProjectId(), false, currentServerMeta.getRevision()));
    }

    @Test
    public void testDeleteUploadedFile() {
        final FileMetaData meta = clientService.delete(user, project, fileMetaData);
        Assertions.assertThat(meta.isDeleted()).isEqualTo(true);
    }

    @Test
    public void testCreateFolder() throws IOException {
        final Random random = new Random();
        final String parentPath = File.separator + random.nextInt(1000000);
        final String path = parentPath + File.separator + random.nextInt(1000000);
        final File folder = new File(project.getRootPath() + path);
        FileUtils.forceMkdir(folder);

        final FileMetaData fileMetaData = clientService.createFolder(user, project, FileMetaData.folder(project.getId(), path, false, 0L));
        Assertions.assertThat(fileMetaData.isFolder()).isTrue();

        final FileMetaData parentMeta = FileMetaData.folder(project.getId(), parentPath, true, fileMetaData.getRevision());
        final FileMetaData delMeta = clientService.delete(user, project, parentMeta);
        Assertions.assertThat(delMeta.isDeleted()).isEqualTo(true);
    }

    @Test
    public void testDeltaResponse() {
        final DeltaResponse deltaResponse = clientService.delta(user, project.getId(), 0L);
        Assertions.assertThat(deltaResponse.getServerMetaDatas().size()).isGreaterThan(50);
    }

    @Test
    public void testDownloadFile() throws IOException {
        final InputStream inStream = clientService.download(user, fileMetaData);
        final String fileContent = IOUtils.toString(inStream);
        IOUtils.closeQuietly(inStream);
        Assertions.assertThat(fileContent).contains("ppt/slideLayouts/slideLayout3.xml");
    }

    @Test
    public void testGetFileMetaData() {

        final FileMetaData metadata = clientService.getCurrentFileMetaData(user, fileMetaData);
        assertThat(metadata.getProjectId()).isEqualTo(fileMetaData.getProjectId());
        assertThat(new SHA2().isValidHash(metadata.getHash())).isEqualTo(true);
        assertThat(metadata.getPath()).isEqualTo(fileMetaData.getPath());
        assertThat(metadata.getRevision()).isNotNull();
    }

    @Test
    public void testFileNotPresent() {
        final FileMetaData metadata = clientService.getCurrentFileMetaData(user, FileMetaData.file(File.separator + "NOT PRESENT","", fileMetaData.getProjectId(), false, 0));
        assertThat(metadata).isNull();
    }

    @Test
    public void testGetFolderMetaData() {
        final FolderMetaData folderMeta = clientService.getFolderMetaData(user, FileMetaData.folder(project.getId(), "/", false,0));
        final FileMetaData rootMeta = folderMeta.getMetaData();
        Assertions.assertThat(rootMeta.getProjectId()).isEqualTo(project.getId());
        Assertions.assertThat(rootMeta.getHash()).isNull();
        Assertions.assertThat(rootMeta.getPath()).isEqualTo("/");
        Assertions.assertThat(rootMeta.getRevision()).isEqualTo(0);
    }

    @Test
    public void testGetProjects() {
        final ProjectResponse projectResponse = clientService.getProjects(user);
        final List<Project> projects = projectResponse.getProjects();
        Assertions.assertThat(projects.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testInvalidUser() {;
        final ProjectResponse projectResponse = clientService.getProjects(new User("invalid", "invalid1215"));
        Assertions.assertThat(projectResponse).isNull();
    }

    @Test
    public void testNotUpToDate() {
        final Map<String,Long> projectIdRevisionMap = new HashMap<String, Long>();
        projectIdRevisionMap.put(project.getId(),0L);

        final ListenForUpdatesResponse listenForUpdatesResponse = clientService.listenForUpdates(user,projectIdRevisionMap, null);
        Assertions.assertThat(listenForUpdatesResponse.getUpdatedProjects().size()).isGreaterThan(0);
    }

    @Test
    public void testUploadTwoRevisionsAndConflict() throws FileNotFoundException {
        final FileMetaData firstMeta = clientService.getCurrentFileMetaData(user,fileMetaData);
        // initial upload
        final UploadResponse initialUploadResponse = clientService.upload(user, project, firstMeta);
        Assertions.assertThat(initialUploadResponse.getConflictedServerMetaData()).isNull();

        final FileMetaData currentMeta = initialUploadResponse.getCurrentServerMetaData();
        // update
        final UploadResponse updateResponse = clientService.upload(user, project, currentMeta);
        Assertions.assertThat(updateResponse.getConflictedServerMetaData()).isNull();

        // conflict
        final UploadResponse conflictResponse = clientService.upload(user, project, fileMetaData);
        Assertions.assertThat(conflictResponse.getConflictedServerMetaData()).isNotNull();

        //remove conflict file
        clientService.delete(user,project,conflictResponse.getConflictedServerMetaData());
    }
}
