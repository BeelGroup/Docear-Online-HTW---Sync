package org.docear.syncdaemon.client;

import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.users.User;

public interface ClientService {
    /**
     *
     */
    UploadResponse upload(FileMetaData fileMetaData);
    void download(FileMetaData currentServerMetaData);
    ProjectResponse getProjects(User user);
    FolderMetaData getFolderMetaData(FileMetaData folderMetaData);
}
