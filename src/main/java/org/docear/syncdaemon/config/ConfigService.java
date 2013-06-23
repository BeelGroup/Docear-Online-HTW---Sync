package org.docear.syncdaemon.config;

import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

import java.io.File;
import java.util.List;

public interface ConfigService {

	List<Project> getProjects();
	void addProject(Project project);
	void deleteProject(Project project);
	String getProjectRootPath(String projectId);
	void saveConfig();
    File getSyncDaemonHome();
    File getDocearHome();
    User getUser();
}
