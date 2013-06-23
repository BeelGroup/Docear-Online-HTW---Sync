package org.docear.syncdaemon.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.NeedsConfig;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.apache.commons.io.FileUtils.getUserDirectory;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class ConfigServiceImpl implements ConfigService, NeedsConfig {

	private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
	//private ProjectCollection projectCollection;
    private Conf localConf;
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
        final String docearHomePath = defaultIfBlank(config.getString("daemon.docear.home"), getUserDirectory() + "/.docear");
        docearHome = new File(docearHomePath);
        syncDaemonHome = new File(docearHome, "projects");
        xmlMapper = new XmlMapper();
        try {
			FileUtils.forceMkdir(syncDaemonHome);
		
			configFile = new File(syncDaemonHome, "config.xml");
			if (!configFile.exists()){
				logger.debug("config file not existing.");
				localConf = new Conf();
			} else {
                //file present
				logger.debug("config file exists: " + configFile.getAbsolutePath());
				localConf = xmlMapper.readValue(configFile, Conf.class);
			}

		} catch (IOException e) {
			logger.error("Error while initialising ConfigServiceImpl in \"" + configFile.getAbsolutePath() + "\".", e);
		}
    }


    @Override
	public List<Project> getProjects() {
		return localConf.getProjects();
	}

	@Override
	public void addProject(Project project) {
        localConf.getProjects().add(project);
		saveConfig();
	}

	@Override
	public void deleteProject(Project project) {
        localConf.getProjects().remove(project);
		saveConfig();
	}

	@Override
	public String getProjectRootPath(String projectId) {
        return localConf.getProjectRootPath(projectId);
    }

	@Override
	public void saveConfig(){
		try {
			if (localConf.getProjects().size() > 0){
				String xml = xmlMapper.writeValueAsString(localConf);
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

    @Override
    public User getUser() {
        return localConf.getUser();
    }
}
