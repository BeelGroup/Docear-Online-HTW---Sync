package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.projects.Project;

public interface FileIndexServiceFactory {
    FileIndexService create(Project project);
}
