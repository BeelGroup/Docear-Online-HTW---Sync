package org.docear.syncdaemon.fileindex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FileReceiver {
	
	private static final Logger logger = LoggerFactory.getLogger(FileReceiver.class);
	public static final HashAlgorithm hashAlgo = new SHA2(); 

	public static List<FileMetaData> receiveFiles(Project project){
		List<FileMetaData> files = new LinkedList();
		File root = new File(project.getRootPath());
		Collection<File> fileCollection = FileUtils.listFilesAndDirs(root, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
		for (File f : fileCollection){
			logger.debug("Processing file \"" + f.getName() + "\" -> \"" + project.toRelativePath(f.getAbsolutePath()) + "\"");
			if (f.equals(root)){
				continue;
			}
			String hash = null;
			try {
				if (f.isFile()) {
					hash = hashAlgo.generate(f);
				}
			} catch (IOException e){
				logger.error(e.toString());
				continue;
			}
			FileMetaData fmd = new FileMetaData(project.toRelativePath(f.getAbsolutePath()), hash, project.getId(), f.isDirectory(), false, -1);
			files.add(fmd);
		}
		return files;
	}
	
	
}
