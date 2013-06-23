package org.docear.syncdaemon.projects;

import org.apache.commons.io.FilenameUtils;

public class Project {
    private String id;
    // rootPath is normalized to current system. there is no end separator
    private String rootPath;
    private long revision;
    
    public Project(){
    	// requiered for jackson xml generation
    }

    public Project(String id, String rootPath, long revision) {
        this.id = id;
        this.revision = revision;
        this.rootPath = FilenameUtils.normalizeNoEndSeparator(FilenameUtils.separatorsToSystem(rootPath));
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
    
    public String toAbsolutePath(final String relativePath){
    	return FilenameUtils.concat(this.rootPath, relativePath);
    }
    
    public String toRelativePath(final String path) {
    	return toRelativePath(this.rootPath, path);
    }
    
    public static String toRelativePath(final String rootPath, final String path) {
    	String rp = FilenameUtils.normalize(FilenameUtils.separatorsToSystem(rootPath));
    	String p = FilenameUtils.normalize(FilenameUtils.separatorsToSystem(path));
    	if (!p.startsWith(rp)){
    		return null;
    	} else {
	    	return p.substring(rp.length());
    	}
    }
}
