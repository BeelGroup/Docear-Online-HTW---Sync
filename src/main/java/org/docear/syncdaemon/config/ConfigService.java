package org.docear.syncdaemon.config;

import java.io.File;
import java.util.List;

import org.docear.syncdaemon.projects.Project;

public interface ConfigService {

	List<Project> getProjects();
	void addProject(Project project);
	void deleteProject(Project project);
	String getProjectRootPath(String projectId);
    File getSyncDaemonHome();
    File getDocearHome();
}
