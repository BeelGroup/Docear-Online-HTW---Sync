package org.docear.syncdaemon.config;

import static org.apache.commons.io.FileUtils.getTempDirectoryPath;
import static org.docear.syncdaemon.TestUtils.testDaemon;
import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.projects.ProjectCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
    	Project project = new Project("thisIsAId", "/root/Path", 8L);
    	service.addProject(project);
    	assertThat(service.getProjects().size()).isEqualTo(1);
    	assertThat(service.getProjectRootPath(project.getId())).isEqualTo(project.getRootPath());
    }
    
    @Test
    public void deleteProject() throws Exception {
    	Project project = new Project("thisIsAId", "/root/Path", 8L);
    	service.addProject(project);
    	service.deleteProject(project);
    	assertThat(service.getProjects().size()).isEqualTo(0);
    	assertThat(service.getProjectRootPath(project.getId())).isNull();
    }
    
    @Test
    @Ignore
    public void saveProject() throws Exception {
    	Project project = new Project("thisIsAId", "/root/Path", 8L);
    	service.saveConfig();
    	String xml = "<ProjectCollection xmlns=\"\"><projects><project><id>thisIsTheId</id><rootPath>/root/Path</rootPath><revision>8</revision></project></projects></ProjectCollection>";
    	XmlMapper xmlMapper = new XmlMapper();
    	File conf = new File(service.getSyncDaemonHome(), "projectsConfig.xml");
    	assertThat(conf.exists()).isTrue();
    	ProjectCollection loadedProject = xmlMapper.readValue(conf, ProjectCollection.class);
    	assertThat(loadedProject).isNotNull();
    	assertThat(loadedProject.getProjects().size()).isEqualTo(1);
    	assertThat(loadedProject.getProjects().get(0).getId()).isEqualTo(project.getId());
    	assertThat(loadedProject.getProjects().get(0).getRevision()).isEqualTo(project.getRevision());
    	assertThat(loadedProject.getProjects().get(0).getRootPath()).isEqualTo(project.getRootPath());
    }
}
