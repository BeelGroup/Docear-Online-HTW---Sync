package org.docear.syncdaemon.indexdb;

import org.docear.syncdaemon.fileindex.FileMetaData;

public class IndexDbServiceImpl implements IndexDbService {

	@Override
	public void save(FileMetaData currentServerMetaData) {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public long getProjectRevision(String projectId) {
		throw new RuntimeException("Not implemented.");
	}

}
