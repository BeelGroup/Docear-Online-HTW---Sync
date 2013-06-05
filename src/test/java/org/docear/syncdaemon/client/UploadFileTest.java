package org.docear.syncdaemon.client;

import java.io.File;
import java.io.FileNotFoundException;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.Ignore;
import org.junit.Test;

public class UploadFileTest {
//	private static final User user = new User("Julius", "Julius-token");
	private static final User user = new User("online-demo", "A91AF9EE20D8611666753B8A49296B5A");


	@Test
	@Ignore
	public void testUploadTwoRevisionsAndConflict() throws FileNotFoundException {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);

		// get root for project
		String pathOfClass = DownloadFileTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";

		final String projectId = "507f191e810c19729de860ea";
		final Project project = new Project(projectId, rootPath, 8);

		// to assure file is not present
		final String filename = "/rootFile.pptx";
		final FileMetaData initialMeta = new FileMetaData(filename, "", projectId, false, false, 0);

		// initial upload
		final UploadResponse initialUploadResponse = clientService.upload(user, project, initialMeta);
		Assertions.assertThat(initialUploadResponse.getConflictedServerMetaData()).isNull();

		// update
		final UploadResponse updateResponse = clientService.upload(user, project, initialMeta);
		Assertions.assertThat(updateResponse.getConflictedServerMetaData()).isNull();

		// conflict
		final UploadResponse conflictResponse = clientService.upload(user, project, initialMeta);
		Assertions.assertThat(conflictResponse.getConflictedServerMetaData()).isNotNull();
	}
}
