package org.docear.syncdaemon.indexdb;

import org.docear.syncdaemon.fileindex.FileMetaData;

public interface IndexDbService {
    public void save(FileMetaData currentServerMetaData);
    public long getProjectRevision(String projectId);
    public FileMetaData getFileMetaData(FileMetaData fileMetaData);
    public FileMetaData getFileMetaData(String pathToFile, String projectId);
}
