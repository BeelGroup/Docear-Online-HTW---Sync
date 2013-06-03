package org.docear.syncdaemon.messages;

public class FileChangeEvent {
    public final String projectId;
    public final String fileInProjectPath;

    public FileChangeEvent(String fileInProjectPath, String projectId) {
        this.fileInProjectPath = fileInProjectPath;
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getFileInProjectPath() {
        return fileInProjectPath;
    }
}