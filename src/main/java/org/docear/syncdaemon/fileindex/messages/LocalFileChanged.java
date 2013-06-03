package org.docear.syncdaemon.fileindex.messages;

import org.docear.syncdaemon.fileindex.FileMetaData;

public class LocalFileChanged {
    private final FileMetaData fileMetaData;

    public LocalFileChanged(FileMetaData currentLocalMetaData) {
        this.fileMetaData = currentLocalMetaData;
    }

    public FileMetaData getFileMetaData() {
        return fileMetaData;
    }
}
