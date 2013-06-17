package org.docear.syncdaemon.projects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProjectCollection {

	private List<Project> projectList;
	private Map<String, String> projectRootPaths;
	
	public ProjectCollection() {
		projectList = new LinkedList<Project>();
		projectRootPaths = new HashMap<String, String>();
	}

	public List<Project> getProjects() {
		return projectList;
	}

	public void addProject(Project project) {
		projectList.add(project);
		projectRootPaths.put(project.getId(), project.getRootPath());
	}

	public void deleteProject(Project project) {
		if (projectList.contains(project))
			projectList.remove(project);
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
