package org.docear.syncdaemon.client;

import org.docear.syncdaemon.fileindex.FileMetaData;

public interface ClientService {
    /**
     *
     */
    UploadResponse upload(FileMetaData fileMetaData);

    void download(FileMetaData currentServerMetaData);
}
