package org.docear.syncdaemon.fileactors;

import org.docear.syncdaemon.fileindex.FileMetaData;

public interface TempFileService {

	int getTimeOutMillis();
	boolean isTempFile(FileMetaData filemetaData);
    boolean isTempFile(String path);
}
