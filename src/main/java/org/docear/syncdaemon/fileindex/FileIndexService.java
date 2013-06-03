package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.projects.Project;

public interface FileIndexService {
    /**
     * scan async projects
     */
    void scanProject(Project project);
}
