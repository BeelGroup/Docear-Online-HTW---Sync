package org.docear.syncdaemon.projects;

public class Project {
    final String id;
    final String rootPath;
    final long revision;

    public Project(String id, String rootPath, long revision) {
        this.id = id;
        this.rootPath = rootPath;
        this.revision = revision;
    }

    public String getId() {
        return id;
    }

    public String getRootPath() {
        return rootPath;
    }
    
    public long getRevision() {
		return revision;
	}
}
