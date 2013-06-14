package org.docear.syncdaemon.client;

import org.apache.commons.io.IOUtils;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DownloadFileITest {
    private static final User user = new User("Julius", "Julius-token");
    private Daemon daemon;
    private ClientService clientService;
    private FileMetaData fileMetaData;

    @Before
    public void setUp() throws FileNotFoundException {
        daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        clientService = daemon.service(ClientService.class);

        //upload a file

        // get root for project
        String pathOfClass = DownloadFileITest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";

        final String projectId = "507f191e810c19729de860ea";
        final Project project = new Project(projectId, rootPath, 8);

        final String filename = File.separator+"rootFile.pptx";
        fileMetaData = new FileMetaData(filename, "", projectId, false, false, 0);

        final UploadResponse initialUploadResponse = clientService.upload(user, project, fileMetaData);
    }

	@Test
	public void testDownloadFile() throws IOException {
		final InputStream inStream = clientService.download(user, fileMetaData);
		final String fileContent = IOUtils.toString(inStream);
		IOUtils.closeQuietly(inStream);
		Assertions.assertThat(fileContent).contains("ppt/slideLayouts/slideLayout3.xml");
	}

}
