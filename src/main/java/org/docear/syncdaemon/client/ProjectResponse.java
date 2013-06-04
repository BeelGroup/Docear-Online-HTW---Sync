package org.docear.syncdaemon.client;

import java.util.List;

import org.docear.syncdaemon.projects.Project;

public class ProjectResponse {
	private final List<Project> projects;
	
	public ProjectResponse(List<Project> projects) {
		this.projects = projects;
	}
	
	public List<Project> getProjects() {
		return projects;
	}
}
