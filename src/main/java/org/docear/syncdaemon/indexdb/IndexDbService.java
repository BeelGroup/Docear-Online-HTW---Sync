package org.docear.syncdaemon.indexdb;

import org.docear.syncdaemon.fileindex.FileMetaData;

public interface IndexDbService {
    /**
     * Saves (inserts or overrides) the meta data in the database.
     * @param currentServerMetaData the data to be stored
     */
    public void save(FileMetaData currentServerMetaData) throws PersistenceException;

    /**
     * returns current project revision
     * @param projectId id of requested project
     */
    public long getProjectRevision(String projectId) throws PersistenceException;

    public void setProjectRevision(String projectId, long revision) throws PersistenceException;

    /**
     * returns current file informations of file in the database, including hash and revision
     * only hash and revision can be different to input metadata
     * @param fileMetaData file metadata of requested file
     */
    public FileMetaData getFileMetaData(FileMetaData fileMetaData) throws PersistenceException;
    
    public void deleteProject(String projectId) throws PersistenceException;
}