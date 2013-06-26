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
        private final String path;

        public FileChangedLocally(Project project, String path) {
            super(project);
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public static final class FileChangedOnServer extends FileChanged {
        private final FileMetaData fileMetaDataOnServer;

        public FileChangedOnServer(Project project, FileMetaData fileMetaDataOnServer) {
            super(project);
            this.fileMetaDataOnServer = fileMetaDataOnServer;
        }

        public FileMetaData getFileMetaDataOnServer() {
            return fileMetaDataOnServer;
        }
    }

    public static class ProjectChange{
        private final Project project;

        public ProjectChange(Project project) {
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }
    
    public static final class ProjectAdded extends ProjectChange {
    	public ProjectAdded(Project project) {
            super(project);
        }
    }

    public static final class ProjectUpdated extends ProjectChange {
    	public ProjectUpdated(Project project) {
            super(project);
        }
    }
    
    public static final class ProjectDeleted extends ProjectChange {
    	public ProjectDeleted(Project project) {
            super(project);
        }
    }

    public static final class StartListening {
        private Map<String, Long> projectIdRevisionMap;

        public StartListening(Map<String, Long> projectIdRevisionMap) {
            this.projectIdRevisionMap = projectIdRevisionMap;
        }

        public Map<String, Long> getProjectIdRevisionMap() {
            return projectIdRevisionMap;
        }
    }
    
    public static final class ListenAgain {
    	
    }
}
