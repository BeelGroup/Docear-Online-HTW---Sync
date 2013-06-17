package org.docear.syncdaemon.config;

import static org.apache.commons.io.FileUtils.getUserDirectory;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.NeedsConfig;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.projects.ProjectCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.typesafe.config.Config;

public class ConfigServiceImpl implements ConfigService, NeedsConfig {

	private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
	private ProjectCollection projects;
	private File syncDaemonHome;
	private File configFile;
	private XmlMapper xmlMapper;
    private Config config;
    private File docearHome;

    public ConfigServiceImpl() {
	}

    @Override
    public void setConfig(Config config) {
        this.config = config;
        init();
    }

    private void init() {
        final String docearHomePath = defaultString(config.getString("daemon.docear.home"), getUserDirectory() + "/.docear");
        docearHome = new File(docearHomePath);
        syncDaemonHome = new File(docearHome, "projects");
        xmlMapper = new XmlMapper();
        try {
			FileUtils.forceMkdir(syncDaemonHome);
		
			configFile = new File(syncDaemonHome, "projectsConfig.xml");
			if (!configFile.exists()){
				logger.debug("config file not existing.");
				projects = new ProjectCollection();
			} else {
				logger.debug("config file exists.");
				// TODO fix read error
				projects = xmlMapper.readValue(configFile, ProjectCollection.class);
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
		saveConfig();
	}

	@Override
	public void deleteProject(Project project) {
		projects.deleteProject(project);
		saveConfig();
	}

	@Override
	public String getProjectRootPath(String projectId) {
		return projects.getProjectRootPath(projectId);
	}

	@Override
	public void saveConfig(){
		try {
			if (projects.getProjects().size() > 0){
				String xml = xmlMapper.writeValueAsString(projects);
				FileUtils.writeStringToFile(configFile, xml);
			} else {
				if (configFile.exists())
					FileUtils.forceDelete(configFile);
			}
		} catch (JsonProcessingException e) {
			logger.error("Error while mapping xml.", e);
		} catch (IOException e) {
			logger.error("Error while saving file.", e);
		}
	}

    @Override
    public File getSyncDaemonHome() {
        return syncDaemonHome;
    }

    @Override
    public File getDocearHome() {
        return docearHome;
    }
}
