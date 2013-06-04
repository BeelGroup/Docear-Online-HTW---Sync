package org.docear.syncdaemon.projects;

public class Project {
    final String id;
    final String rootPath;
    final long revision;

    public Project(String id, String rootPath, long revision) {
    	// TODO assume rootpath ends with "/"
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
    
    public String toRelativePath(final String path) {
    	return toProjectRelativePath(this.rootPath, path);
    }
    
    public static String toProjectRelativePath(final String rootPath, final String path) {
    	if (!path.startsWith(rootPath)){
    		return null;
    	} else {
	    	return path.substring(rootPath.length());
    	}
    }
}
