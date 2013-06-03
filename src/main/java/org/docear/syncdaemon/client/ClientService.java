package org.docear.syncdaemon.client;

import org.docear.syncdaemon.fileindex.FileMetaData;

public interface ClientService {
    /**
     * 
     */
    FileMetaData getFileMetaData(FileMetaData fileMetaData);
}
