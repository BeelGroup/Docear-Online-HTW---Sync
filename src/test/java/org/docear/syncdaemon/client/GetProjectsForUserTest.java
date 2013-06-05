package org.docear.syncdaemon.client;

import java.util.List;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.Ignore;
import org.junit.Test;

public class GetProjectsForUserTest {
	private static final User user = new User("Julius", "Julius-token");

	@Test
	@Ignore
	public void testGetProjects() {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final ProjectResponse projectResponse = clientService.getProjects(user);
		final List<Project> projects = projectResponse.getProjects();
		Assertions.assertThat(projects).hasSize(2);
	}

	@Test
	@Ignore
	public void testInvalidUser() {
		final Daemon deamon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
		final ClientService clientService = deamon.service(ClientService.class);
		final ProjectResponse projectResponse = clientService.getProjects(new User("invalid", "invalid1215"));
		Assertions.assertThat(projectResponse).isNull();
	}
}
