package org.docear.syncdaemon.client;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class UploadFileTest {
	private static final User user = new User("Julius", "Julius-token");
    private Daemon daemon;
    private ClientService clientService;
    private Project project;
    private FileMetaData fileMetaData;

    @Before
    public void setUp() {

        daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        clientService = daemon.service(ClientService.class);

        String pathOfClass = DownloadFileTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";
        final String projectId = "507f191e810c19729de860ea";
        project = new Project(projectId, rootPath, 8);

        final String filename = "/rootFile.pptx";
        fileMetaData = new FileMetaData(filename, "", projectId, false, false, 0);

        FileMetaData deletedMeta =  clientService.delete(user,project,fileMetaData);
        if(deletedMeta != null)
            Assertions.assertThat(deletedMeta.isDeleted()).isEqualTo(true);

        clientService.delete(user,project,new FileMetaData("/rootFile(Conflicted Version 1).pptx","",projectId,false,true,0));
    }

    @After
    public void tearDown() {
        FileMetaData deletedMeta =  clientService.delete(user,project,fileMetaData);
        if(deletedMeta != null)
            Assertions.assertThat(deletedMeta.isDeleted()).isEqualTo(true);
    }

	@Test
    @Ignore
	public void testUploadTwoRevisionsAndConflict() throws FileNotFoundException {
		// initial upload
		final UploadResponse initialUploadResponse = clientService.upload(user, project, fileMetaData);
		Assertions.assertThat(initialUploadResponse.getConflictedServerMetaData()).isNull();

        final FileMetaData currentMeta = initialUploadResponse.getCurrentServerMetaData();
		// update
		final UploadResponse updateResponse = clientService.upload(user, project, currentMeta);
		Assertions.assertThat(updateResponse.getConflictedServerMetaData()).isNull();

		// conflict
		final UploadResponse conflictResponse = clientService.upload(user, project, fileMetaData);
		Assertions.assertThat(conflictResponse.getConflictedServerMetaData()).isNotNull();
	}
}
