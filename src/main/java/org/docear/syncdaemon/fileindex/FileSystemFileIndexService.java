package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.projects.Project;

public class FileSystemFileIndexService implements FileIndexService {
    @Override
    public void scanProject(Project project) {
        //TODO for folders
        final FileMetaData fromScan = null;//TODO this class
        final FileMetaData fromDb = null;//TODO
        if (fromScan.isChanged(fromDb)) {
         //to pipeline to check with server
        } else {
            //akka message if it would be jNotify and life change
        }
    }
}
