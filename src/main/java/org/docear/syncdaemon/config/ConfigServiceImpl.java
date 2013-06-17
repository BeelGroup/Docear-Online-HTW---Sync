package org.docear.syncdaemon.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.projects.ProjectCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class ConfigServiceImpl implements ConfigService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
	private ProjectCollection projects;
	private File syncDaemonHome;
	private File config;
	private XmlMapper xmlMapper;
	
	public ConfigServiceImpl() {
		xmlMapper = new XmlMapper();
		
		File docearHome = new File(FileUtils.getUserDirectory(), ".docear");
		syncDaemonHome = new File(docearHome, "projects");
		try {
			FileUtils.forceMkdir(syncDaemonHome);
		
			config = new File(syncDaemonHome, "projectsConfig.xml");
			if (!config.exists()){
				logger.debug("config file not existing.");
				projects = new ProjectCollection();
			} else {
				logger.debug("config file exists.");
				// TODO fix read error
				//projects = xmlMapper.readValue(config, ProjectCollection.class);
			}
		} catch (IOException e) {
			logger.error("Error while initialising ConfigServiceImpl.", e);
		}
	}
	
	@Override
	public List<Project> getProjects() {
		return projects.getProjects();
	}

	@Override
	public void addProject(Project project) {
		projects.addProject(project);
		
		// TODO save project information to config file in user folder
	}

	@Override
	public void deleteProject(Project project) {
		projects.deleteProject(project);
	
		// TODO delete project information to config file in user folder	
	}

	@Override
	public String getProjectRootPath(String projectId) {
		return projects.getProjectRootPath(projectId);
	}

	@Override
	public void saveConfig(){
		try {
			String xml = xmlMapper.writeValueAsString(projects);
			FileUtils.writeStringToFile(config, xml);
		} catch (JsonProcessingException e) {
			logger.error("Error while mapping xml.", e);
		} catch (IOException e) {
			logger.error("Error while saving file.", e);
		}
	}
}
