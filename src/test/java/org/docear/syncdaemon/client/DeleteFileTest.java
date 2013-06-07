package org.docear.syncdaemon.client;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: Julius
 * Date: 07.06.13
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */
public class DeleteFileTest {
    private static final User user = new User("Julius", "Julius-token");
    private Project project;
    private Daemon daemon;
    private ClientService clientService;
    private FileMetaData fileMetaData;

    @Before
    public void setUp() throws FileNotFoundException {
        daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        clientService = daemon.service(ClientService.class);

        //upload a file

        // get root for project
        String pathOfClass = DownloadFileTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";

        final String projectId = "507f191e810c19729de860ea";
        project = new Project(projectId, rootPath, 8);

        final String filename = "/rootFile.pptx";
        fileMetaData = new FileMetaData(filename, "", projectId, false, false, 0);

        final UploadResponse initialUploadResponse = clientService.upload(user, project, fileMetaData);
    }

    @Test
    @Ignore
    public void testDeleteUploadedFile() {
        final FileMetaData meta = clientService.delete(user,project,fileMetaData);
        Assertions.assertThat(meta.isDeleted()).isEqualTo(true);
    }
}
