package org.docear.syncdaemon.config;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigServiceImpl implements ConfigService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
	private List<Project> projects;
	private File syncDaemonHome;
	
	public ConfigServiceImpl() {
		projects = new LinkedList<Project>();
		
		File docearHome = new File(FileUtils.getUserDirectory(), ".docear");
		syncDaemonHome = new File(docearHome, "projects");
		try {
			FileUtils.forceMkdir(syncDaemonHome);
		} catch (IOException e) {
			logger.error("Error while initialising ConfigServiceImpl.", e);
		}
		
		// TODO load project information from user folder
	}
	
	@Override
	public List<Project> getProjects() {
		return projects;
	}

	@Override
	public void addProject(Project project) {
		projects.add(project);
		
		// TODO save project information to config file in user folder
	}

	@Override
	public void deleteProject(Project project) {
		projects.remove(project);
		
		// TODO delete project information to config file in user folder	
	}

	@Override
	public String getProjectRootPath(String projectId) {
		throw new RuntimeException("Not implemented.");
	}

}
