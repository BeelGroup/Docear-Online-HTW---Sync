package org.docear.syncdaemon.fileindex;

public final class FileMetaData {
    /**
     * the absolute path from project
     */
    final String path;
    final String hash;
    final String projectId;
    final boolean isFolder;
    final boolean isDeleted;
    /**
     * the current revision or the last local revision
     */
    final long revision;

    public FileMetaData(String path, String hash, String projectId, boolean folder, boolean deleted, long revision) {
        this.path = path;
        this.hash = hash;
        this.projectId = projectId;
        isFolder = folder;
        isDeleted = deleted;
        this.revision = revision;
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

    public boolean isChanged(FileMetaData other) {
        if (other.projectId != projectId) {
            throw new IllegalArgumentException("compared files between different projects");
        }
        if (other.path != path) {
            throw new IllegalArgumentException("compared files for different paths");
        }
        return hash != other.hash;
    }
}
