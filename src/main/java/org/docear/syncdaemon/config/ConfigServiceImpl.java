package org.docear.syncdaemon.config;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.docear.syncdaemon.NeedsConfig;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.io.FileUtils.getUserDirectory;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class ConfigServiceImpl implements ConfigService, NeedsConfig {

	private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
	private List<Project> projects;
	private File syncDaemonHome;
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
        projects = new LinkedList<Project>();

        final String docearHomePath = defaultIfBlank(config.getString("daemon.docear.home"), getUserDirectory() + "/.docear");
        docearHome = new File(docearHomePath);
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

    @Override
    public File getSyncDaemonHome() {
        return syncDaemonHome;
    }

    @Override
    public File getDocearHome() {
        return docearHome;
    }

}
