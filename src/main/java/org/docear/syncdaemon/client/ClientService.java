package org.docear.syncdaemon.client;

import java.io.FileNotFoundException;

import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

public interface ClientService {
    /**
     *
     */
    UploadResponse upload(Project project, FileMetaData fileMetaData) throws FileNotFoundException;
    void download(FileMetaData currentServerMetaData);
    ProjectResponse getProjects(User user);
    FolderMetaData getFolderMetaData(FileMetaData folderMetaData);
    
    /**
     * @return {@link FileMetaData} or <code>null</code> if not present
     */
    FileMetaData getCurrentFileMetaData(FileMetaData fileMetaData);
}
