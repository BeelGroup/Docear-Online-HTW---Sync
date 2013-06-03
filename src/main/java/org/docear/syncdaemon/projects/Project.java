package org.docear.syncdaemon.projects;

public class Project {
    final String id;
    final String rootPath;

    public Project(String id, String rootPath) {
        this.id = id;
        this.rootPath = rootPath;
    }

    public String getId() {
        return id;
    }

    public String getRootPath() {
        return rootPath;
    }
}
