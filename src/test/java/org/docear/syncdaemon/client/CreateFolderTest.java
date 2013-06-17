package org.docear.syncdaemon.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CreateFolderTest {

    private static final User user = new User("Julius", "Julius-token");
    private Project project;
    private Daemon daemon;
    private ClientService clientService;

    @Before
    public void setUp() throws FileNotFoundException {
        daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        clientService = daemon.service(ClientService.class);

        // get root for project
        String pathOfClass = DeleteFileITest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";

        final String projectId = "507f191e810c19729de860ea";
        project = new Project(projectId, rootPath, 0);
    }

    @Test
    @Ignore
    public void testCreateFolder() throws FileNotFoundException {
        final Random  random = new Random();
        final String path = File.separator + random.nextInt(1000000) + File.separator + random.nextInt(1000000);
        final File folder = new File(project.getRootPath()+path);
        folder.mkdirs();
        final FileMetaData fileMetaData = clientService.createFolder(user, project, FileMetaData.folder(project.getId(),path,false,0L));
        Assertions.assertThat(fileMetaData.isFolder()).isTrue();
    }
}
