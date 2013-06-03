package org.docear.syncdaemon.client;

import org.docear.syncdaemon.fileindex.FileMetaData;

public class ClientServiceImpl implements ClientService {
    @Override
    public UploadResponse upload(FileMetaData fileMetaData) {
        //Problem: inkrementierung Projekt Revision?
        if (versionFromServer > currentVersion) {
            //conflicted version, load current file, return metaDataCurrentVersionFromServer
        } else if(versionFromServer == currentVersion){
            upload //return newFileMetaData
        } else {
            //throw error
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
