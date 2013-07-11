package org.docear.syncdaemon.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.projects.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.apache.commons.io.FileUtils.getTempDirectoryPath;
import static org.docear.syncdaemon.TestUtils.testDaemon;
import static org.fest.assertions.Assertions.assertThat;

public class ConfigServiceImplTest {
    private ConfigService service;
    private Daemon daemon;

    private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImplTest.class);
    
    @Before
    public void setUp() throws Exception {
        daemon = testDaemon();
        daemon.onStart();
        service = daemon.service(ConfigService.class);
    }

    @After
    public void tearDown() throws Exception {
        daemon.onStop();
        daemon = null;
        service = null;
    }

    @Test
    public void testInit() throws Exception {
        assertThat(service.getDocearHome()).exists();
        assertThat(service.getSyncDaemonHome()).exists();
        assertThat(service.getSyncDaemonHome()).isDirectory();
        assertThat(service.getSyncDaemonHome().getAbsolutePath()).overridingErrorMessage("in test daemon home is in the tmp folder").contains(getTempDirectoryPath());
    }
    
    @Test
    public void addProject() throws Exception {
    	Project project = new Project("thisIsAId", "/root/Path", 8L, "name");
    	service.addProject(project);
    	assertThat(service.getProjects().size()).isEqualTo(1);
    	assertThat(service.getProjectRootPath(project.getId())).isEqualTo(project.getRootPath());
    }
    
    @Test
    public void deleteProject() throws Exception {
    	Project project = new Project("thisIsAId", "/root/Path", 8L, "name");
    	service.addProject(project);
    	service.deleteProject(project);
    	assertThat(service.getProjects().size()).isEqualTo(0);
    	assertThat(service.getProjectRootPath(project.getId())).isNull();
    }
    
    @Test
    public void saveProject() throws Exception {
    	Project project = new Project("thisIsTheId", "/root/Path", 8L, "name");
    	service.addProject(project);
    	service.saveConfig();
    	XmlMapper xmlMapper = new XmlMapper();
    	File conf = new File(service.getSyncDaemonHome(), "config.xml");
    	assertThat(conf.exists()).isTrue();
    	Conf localConf = xmlMapper.readValue(conf, Conf.class);
    	assertThat(localConf).isNotNull();
    	assertThat(localConf.getProjects().size()).isEqualTo(1);
    	assertThat(localConf.getProjects().get(0).getId()).isEqualTo(project.getId());
    	assertThat(localConf.getProjects().get(0).getRevision()).isEqualTo(project.getRevision());
    	assertThat(localConf.getProjects().get(0).getRootPath()).isEqualTo(project.getRootPath());
    }
}
