package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class FileMetaData {
    final static Logger logger = LoggerFactory.getLogger(FileMetaData.class);
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
        this.path = normalizePath(path);
        this.hash = hash;
        this.projectId = projectId;
        this.isFolder = isFolder;
        this.isDeleted = isDeleted;
        this.revision = revision;
    }
    
    public static FileMetaData folder(String projectId, String path, boolean isDeleted, long revision) {
        return new FileMetaData(path, null, projectId, true, isDeleted, revision);
    }

    public static FileMetaData file(String path, String hash, String projectId, boolean isDeleted, long revision) {
        return new FileMetaData(path, hash, projectId, false, isDeleted, revision);
    }

    public static FileMetaData newFile(String path, String hash, String projectId) {
        return new FileMetaData(path, hash, projectId, false, false, 0);
    }

    public static FileMetaData fromFS(HashAlgorithm hashAlgorithm, String projectId, String path, String name) {
        File f = new File(path, name);

        boolean isDeleted = !f.exists();
        boolean isDirectory = f.isDirectory();
        String hash = "";
        if (!isDeleted && !isDirectory) {

            try {
                while (hash.equals("")) {
                    try {
                        hash = hashAlgorithm.generate(f);
                    } catch (FileNotFoundException e) {
                        //do nothing
                    }
                }
            } catch (IOException e) {
                logger.error("Couldn't create Hash for FileMetaData for file \"" + name + "\".", e);
            }
        }
        FileMetaData fileMetaData = new FileMetaData(
                "/" + name,
                hash,
                projectId,
                isDirectory,
                isDeleted,
                -1);
        logger.debug(fileMetaData.toString());
        return fileMetaData;
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
        if (!other.projectId.equals(projectId)) {
            throw new IllegalArgumentException("compared files between different projects");
        }
        if (!other.path.equals(path)) {
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

    private String normalizePath(String path) {
        String nPath = path.replace('\\','/');
        nPath = nPath.replace("//","/");

        if(nPath.length() > 1 && !nPath.startsWith("/")) {
            nPath = "/"+nPath;
        }
        if(nPath.endsWith("/") && nPath.length() > 1)
            nPath = nPath.substring(0, nPath.length()-1);
        return nPath;
    }
}
