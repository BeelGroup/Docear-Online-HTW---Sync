package org.docear.syncdaemon.fileactors;


import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;

import java.util.Map;

public class Messages {

    private static class FileChanged {
        private final Project project;


        public FileChanged(Project project) {
            this.project = project;
        }

        public Project getProject() {
            return project;
        }

    }

    public static final class FileChangedLocally extends FileChanged {
        private final FileMetaData fileMetaDataLocally;

        public FileChangedLocally(Project project, FileMetaData fileMetaData) {
            super(project);
            this.fileMetaDataLocally = fileMetaData;
        }

        public FileMetaData getFileMetaDataLocally() {
            return fileMetaDataLocally;
        }
    }

    public static final class FileChangedOnServer extends FileChanged {
        private final FileMetaData fileMetaDataOnServer;

        public FileChangedOnServer(Project project, FileMetaData fileMetaDataOnServer) {
            super(project);
            this.fileMetaDataOnServer = fileMetaDataOnServer;
        }
    }

    public static final class ProjectAdded {
        private final String projectId;

        public ProjectAdded(String projectId) {

            this.projectId = projectId;
        }

        public String getProjectId() {
            return projectId;
        }
    }

    public static final class ProjectDeleted {
        private final String projectId;

        public ProjectDeleted(String projectId) {

            this.projectId = projectId;
        }

        public String getProjectId() {
            return projectId;
        }
    }

    public static final class StartListening {
        private Map<String, Long> projectIdRevisionMap;

        public Map<String, Long> getProjectIdRevisionMap() {
            return projectIdRevisionMap;
        }

        public StartListening(Map<String, Long> projectIdRevisionMap) {

            this.projectIdRevisionMap = projectIdRevisionMap;
        }
    }
}
