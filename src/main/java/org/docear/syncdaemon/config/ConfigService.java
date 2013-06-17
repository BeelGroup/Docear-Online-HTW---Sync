package org.docear.syncdaemon.config;

import java.util.List;

import org.docear.syncdaemon.projects.Project;

public interface ConfigService {

	List<Project> getProjects();
	void addProject(Project project);
	void deleteProject(Project project);
	String getProjectRootPath(String projectId);
}
