package org.docear.syncdaemon.client;


import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class DeltaITest {
    private static final User user = new User("Julius", "Julius-token");
    private Project project;
    private Daemon daemon;
    private ClientService clientService;

    @Before
    public void setUp() throws FileNotFoundException {
        daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
        clientService = daemon.service(ClientService.class);

        //upload a file

        // get root for project
        String pathOfClass = DeleteFileITest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";

        final String projectId = "507f191e810c19729de860ea";
        project = new Project(projectId, rootPath, 0);
    }

    @Test
    public void testDeltaResponse() {
        final DeltaResponse deltaResponse = clientService.delta(user, project.getId(), project.getRevision());
        Assertions.assertThat(deltaResponse.getServerMetaDatas().size()).isGreaterThan(5);
    }
}
