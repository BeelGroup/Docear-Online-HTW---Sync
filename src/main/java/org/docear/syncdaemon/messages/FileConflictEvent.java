package org.docear.syncdaemon.messages;

public class FileConflictEvent {
    public final String projectId;
    public final String fileInProjectPath;
    // TODO add required fields for conflict

    public FileConflictEvent(String fileInProjectPath, String projectId) {
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
