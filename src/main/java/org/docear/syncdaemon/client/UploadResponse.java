package org.docear.syncdaemon.client;

import org.docear.syncdaemon.fileindex.FileMetaData;

public class UploadResponse {
    private final FileMetaData currentServerMetaData;
    /**
     * optional
     */
    private final FileMetaData conflictedServerMetaData;

    public UploadResponse(FileMetaData currentServerMetaData) {
    	this(currentServerMetaData,null);
    }
    
    public UploadResponse(FileMetaData currentServerMetaData, FileMetaData conflictedServerMetaData) {
        this.currentServerMetaData = currentServerMetaData;
        this.conflictedServerMetaData = conflictedServerMetaData;
    }

    public FileMetaData getCurrentServerMetaData() {
        return currentServerMetaData;
    }

    public FileMetaData getConflictedServerMetaData() {
        return conflictedServerMetaData;
    }

    public boolean hasConflicts() {
        return getConflictedServerMetaData() != null;
    }
}
