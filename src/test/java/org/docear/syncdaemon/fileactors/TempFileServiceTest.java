package org.docear.syncdaemon.fileactors;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.docear.syncdaemon.fileindex.FileMetaData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TempFileServiceTest {
	
	private Config config;
	private TempFileServiceImpl service;
	
	@Before
	public void setUp(){
		config = ConfigFactory.parseString("daemon.tmpfiles.millis=500\ndaemon.tmpfiles.regex=[\"tmp\", \"BAK\",\"kate-swp\"]");
		service = new TempFileServiceImpl();
		service.setConfig(config);
	}
	
	@After
	public void tearDown(){
		service = null;
		config = null;
	}
	
	@Test
	public void testTempFileServiceRealFile(){
		File file = new File("noTmpFile.mm");
		FileMetaData fileMetaData = FileMetaData.newFile(file.getPath(), "thisIsTheHash", "thisIsTheProjectId");
		
		assertThat(service.isTempFile(fileMetaData)).isFalse();
	}
	
	@Test
	public void testTempFileService(){
		File file = new File("tmpFile.tmp");
		FileMetaData fileMetaData = FileMetaData.newFile(file.getPath(), "thisIsTheHash", "thisIsTheProjectId");
		
		assertThat(service.isTempFile(fileMetaData)).isTrue();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertThat(service.isTempFile(fileMetaData)).isTrue();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertThat(service.isTempFile(fileMetaData)).isFalse();
	}
    
}
