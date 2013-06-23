package org.docear.syncdaemon.projects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProjectCollection {

	private List<Project> projects;
	private Map<String, String> projectRootPaths;
	
	public ProjectCollection() {
		projects = new LinkedList<Project>();
		projectRootPaths = new HashMap<String, String>();
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void addProject(Project project) {
		projects.add(project);
		projectRootPaths.put(project.getId(), project.getRootPath());
	}

	public void deleteProject(Project project) {
		if (projects.contains(project))
			projects.remove(project);
		if (projectRootPaths.containsKey(project.getId()))
			projectRootPaths.remove(project.getId());	
	}

	public String getProjectRootPath(String projectId) {
		if (projectRootPaths.containsKey(projectId)){
			return projectRootPaths.get(projectId);
		}
		return null;
	}

}
