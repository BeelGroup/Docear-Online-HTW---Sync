package org.docear.syncdaemon.fileindex.messages;

import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;

public class LocalFileChanged {
    private final FileMetaData fileMetaData;
    private final Project project;

    public LocalFileChanged(Project project, FileMetaData currentLocalMetaData) {
        this.fileMetaData = currentLocalMetaData;
        this.project = project;
    }

    public FileMetaData getFileMetaData() {
        return fileMetaData;
    }

	public Project getProject() {
		return project;
	}
}
