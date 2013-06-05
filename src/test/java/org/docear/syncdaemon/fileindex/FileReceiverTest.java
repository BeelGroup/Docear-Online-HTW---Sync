package org.docear.syncdaemon.fileindex;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.projects.Project;
import org.junit.Test;

public class FileReceiverTest {

	@Test
	public void testReceiveFiles() throws IOException{
		String projectId = "-1";
		
		String projectPath = System.getProperty("user.dir") + "/" + "src/test/resources/Testprojects/Project_0";
		Project project0 = new Project(projectId, projectPath, -1);
		
		// force empty folders to be available
		FileUtils.forceMkdir(new File(projectPath + "/folder0/folder01"));
		FileUtils.forceMkdir(new File(projectPath + "/folder1/empty_folder"));
		
		List<FileMetaData> files = FileReceiver.receiveFiles(project0);
		assertThat(files).isNotEmpty();
		assertThat(files.size()).isEqualTo(21);
		
		HashAlgorithm hashAlgo = new SHA2();
		File file = new File(projectPath + "/rootFile.pptx");
		FileMetaData md0 = new FileMetaData("/rootFile.pptx", hashAlgo.generate(file), projectId, false, false, -1);
		FileMetaData md1 = new FileMetaData("/folder0", null, projectId, true, false, -1);
		
		int matchCounter = 0;
		for (FileMetaData fmd : files){
			if (md0.equals(fmd) || md1.equals(fmd)){
				matchCounter++;
			}
		}
		assertThat(matchCounter).isEqualTo(2);
	}
}
