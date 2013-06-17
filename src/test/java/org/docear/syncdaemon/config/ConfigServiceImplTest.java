package org.docear.syncdaemon.config;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.projects.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.io.FileUtils.getTempDirectoryPath;
import static org.docear.syncdaemon.TestUtils.testDaemon;
import static org.fest.assertions.Assertions.assertThat;

public class ConfigServiceImplTest {
    private ConfigService service;
    private Daemon daemon;

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
}
