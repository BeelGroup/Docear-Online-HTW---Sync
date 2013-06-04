package org.docear.syncdaemon.projects;

import java.util.List;

public interface LocalProjectService {
	
	// return list of local projects
    public List<Project> getProjects();
}
