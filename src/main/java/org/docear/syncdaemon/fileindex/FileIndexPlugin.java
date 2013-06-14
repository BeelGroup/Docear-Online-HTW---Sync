package org.docear.syncdaemon.fileindex;

import java.util.List;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ClientServiceImpl;
import org.docear.syncdaemon.fileactors.FileChangeActor;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.h2.H2IndexDbService;
import org.docear.syncdaemon.projects.LocalProjectService;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

public class FileIndexPlugin extends Plugin{

	public FileIndexPlugin(Daemon daemon) {
		super(daemon);
	}

	@Override
	public void onStart() {
        final LocalProjectService projectService = daemon().service(LocalProjectService.class);
        final ClientService clientService = daemon().service(ClientServiceImpl.class);
        final IndexDbService indexDbService = daemon().service(H2IndexDbService.class);
        final List<Project> projects = projectService.getProjects();
        final FileIndexServiceFactory factory = daemon().service(FileIndexServiceFactoryImpl.class);
        final User user = daemon().getUser();
        final FileChangeActor fileChangeActor = new FileChangeActor(clientService, indexDbService, user);
        for (final Project project : projects) {
            factory.create(project, fileChangeActor.getSelf());
        }
    }

	@Override
	public void onStop() {
	
	}
}
