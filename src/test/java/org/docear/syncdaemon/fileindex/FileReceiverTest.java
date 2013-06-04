package org.docear.syncdaemon.fileindex;

import java.util.List;

import org.docear.syncdaemon.projects.Project;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class FileReceiverTest {

	@Test
	public void testReceiveFiles(){
		String projectId = "-1";
		String projectPath = "./src/test/resources/Testprojects/Project_0";
		Project project0 = new Project(projectId, projectPath, -1);
		List<FileMetaData> files = FileReceiver.receiveFiles(project0);
		assertThat(files).isNotEmpty();
		assertThat(files.size()).isEqualTo(21);
		FileMetaData metaDataFolder0 = new FileMetaData(projectId, "folder0", false);
		FileMetaData metaDataFolder1 = new FileMetaData(projectId, "folder1", false);
		FileMetaData metaDataFolder2 = new FileMetaData(projectId, "folder2", false);
	}
}
