package org.docear.syncdaemon.client;

import com.typesafe.config.Config;
import org.docear.syncdaemon.NeedsConfig;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.users.User;

public class ClientServiceImpl implements ClientService, NeedsConfig {
    private Config config;

    @Override
    public UploadResponse upload(FileMetaData fileMetaData) {
    	throw new RuntimeException("Not implemented.");
    	/*
        //Problem: inkrementierung Projekt Revision?
        if (versionFromServer > currentVersion) {
            //conflicted version, load current file, return metaDataCurrentVersionFromServer
        } else if(versionFromServer == currentVersion){
            upload //return newFileMetaData
        } else {
            //throw error
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
        */
    }

	@Override
	public void download(FileMetaData currentServerMetaData) {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public ProjectResponse getProjects(User user) {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public FolderMetaData getFolderMetaData(FileMetaData folderMetaData) {
		throw new RuntimeException("Not implemented.");
	}

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }
}