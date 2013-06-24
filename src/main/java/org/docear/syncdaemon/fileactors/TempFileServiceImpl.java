package org.docear.syncdaemon.fileactors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docear.syncdaemon.NeedsConfig;
import org.docear.syncdaemon.fileindex.FileMetaData;

import com.typesafe.config.Config;

public class TempFileServiceImpl implements TempFileService, NeedsConfig {
	
	private int timeOutMillis;
	private List<String> tmpFileRegex;
	
	private Map<String, Long> timedOutFiles;
	
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
		if (timedOutFiles.containsKey(path) && timedOutFiles.get(path) < (System.currentTimeMillis() + timeOutMillis)){
			timedOutFiles.remove(path);
			return false;
		}
		
		for (String regex : tmpFileRegex){
			if (filemetaData.getPath().matches(regex)){
				timedOutFiles.put(filemetaData.getPath(), System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}

	@Override
	public void setConfig(Config config) {
		this.timeOutMillis = config.getInt("daemon.tmpfiles.millis");
		this.tmpFileRegex = config.getStringList("daemon.tmpfiles.regex");
	}

}
