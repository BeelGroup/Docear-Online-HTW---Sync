package org.docear.syncdaemon.fileactors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.docear.syncdaemon.NeedsConfig;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

public class TempFileServiceImpl implements TempFileService, NeedsConfig {
	
	private int timeOutMillis;
	private List<String> tmpFileExtensions;
	
	private Map<String, Long> timedOutFiles;
	
	private static final Logger logger = LoggerFactory.getLogger(TempFileServiceImpl.class);
	
	public TempFileServiceImpl() {
		this.timedOutFiles = new HashMap<String, Long>();
	}

	@Override
	public int getTimeOutMillis() {
		return timeOutMillis;
	}

	@Override
	public boolean isTempFile(FileMetaData filemetaData) {
		String path = filemetaData.getPath();
		
		// file already timed out long enough. The file is no temp file any more.
		if (timedOutFiles.containsKey(path)){
			if ((timedOutFiles.get(path) + timeOutMillis) < System.currentTimeMillis()){
				timedOutFiles.remove(path);
				return false;
			} else {
				return true;
			}
		} 
		
		if (FilenameUtils.isExtension(path, tmpFileExtensions)){
			timedOutFiles.put(path, System.currentTimeMillis());
			return true;		
		}
		
		return false;
	}

	@Override
	public void setConfig(Config config) {
		this.timeOutMillis = config.getInt("daemon.tmpfiles.millis");	
		this.tmpFileExtensions = config.getStringList("daemon.tmpfiles.regex");
	}

}
