package org.docear.syncdaemon.fileindex;

import org.apache.commons.io.FilenameUtils;

public final class FileMetaData {
	// path always starts with "/"
    final String path;
    final String hash;
    final String projectId;
    final boolean isFolder;
    final boolean isDeleted;
    /**
     * the current revision or the last local revision
     */
    final long revision;

    public FileMetaData(String path, String hash, String projectId, boolean isFolder, boolean isDeleted, long revision) {
        this.path = FilenameUtils.normalizeNoEndSeparator(FilenameUtils.separatorsToSystem(path));
        this.hash = hash;
        this.projectId = projectId;
        this.isFolder = isFolder;
        this.isDeleted = isDeleted;
        this.revision = revision;
    }
    
    public static FileMetaData folder(String projectId, String path, boolean isDeleted) {
        return new FileMetaData(path, null, projectId, true, isDeleted, 0);
    }

    public static FileMetaData file(String path, String hash, String projectId, boolean isDeleted, long revision) {
        return new FileMetaData(path, hash, projectId, false, isDeleted, revision);
    }

    public static FileMetaData newFile(String path, String hash, String projectId) {
        return new FileMetaData(path, hash, projectId, false, false, 0);
    }
    
    public String getPath() {
        return path;
    }

    public String getHash() {
        return hash;
    }

    public String getProjectId() {
        return projectId;
    }

    public long getRevision() {
        return revision;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public boolean isChanged(FileMetaData other) {
        if (other.projectId != projectId) {
            throw new IllegalArgumentException("compared files between different projects");
        }
        if (other.path != path) {
            throw new IllegalArgumentException("compared files for different paths");
        }
        return hash != other.hash;
    }
    
    @Override
    public String toString(){
		return "path: " + path 
				+ " hash: " + hash
				+ " projectId: " + projectId 
				+ " isFolder: " + isFolder
				+ " isDeleted: " + isDeleted
				+ " revision: " + revision;
    }
    
    @Override
    public boolean equals(Object obj) {
    	FileMetaData other = (FileMetaData) obj;
    	return this.isDeleted == other.isDeleted 
    		&& this.isFolder == other.isFolder
    		&& ((this.hash == null && other.hash== null) || (this.hash.equals(other.hash)))
    		&& this.path.equals(other.path)
    		&& this.projectId.equals(other.projectId)
    		&& this.revision == other.revision;
    }
}
