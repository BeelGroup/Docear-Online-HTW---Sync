package org.docear.syncdaemon.client;

import org.docear.syncdaemon.fileindex.FileMetaData;

import java.util.List;

public class DeltaResponse {
    private final String projectId;
    private final Long revisionOnServer;
    private final List<FileMetaData> serverMetaDatas;

    public DeltaResponse(String projectId, Long revisionOnServer, List<FileMetaData> serverMetaDatas) {
        this.projectId = projectId;

        this.revisionOnServer = revisionOnServer;
        this.serverMetaDatas = serverMetaDatas;
    }

    public Long getRevisionOnServer() {
        return revisionOnServer;
    }

    public List<FileMetaData> getServerMetaDatas() {
        return serverMetaDatas;
    }

    public String getProjectId() {
        return projectId;
    }
}
