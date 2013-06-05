package org.docear.syncdaemon.client;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

public interface ClientService {
    /**
     *
     */
    UploadResponse upload(User user, Project project, FileMetaData fileMetaData) throws FileNotFoundException;
    InputStream download(User user, FileMetaData currentServerMetaData);
    ProjectResponse getProjects(User user);
    FolderMetaData getFolderMetaData(User user, FileMetaData folderMetaData);
    
    /**
     * @return {@link FileMetaData} or <code>null</code> if not present
     */
    FileMetaData getCurrentFileMetaData(User user, FileMetaData fileMetaData);
}
