package org.docear.syncdaemon.fileindex;

import akka.actor.ActorRef;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.config.ConfigService;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FileIndexPlugin extends Plugin{
    private static final Logger logger = LoggerFactory.getLogger(FileIndexPlugin.class);

	public FileIndexPlugin(Daemon daemon) {
		super(daemon);
	}

	@Override
	public void onStart() {
        logger.info("FileIndex Plugin starting...");
        final ConfigService projectService = daemon().service(ConfigService.class);
        final ClientService clientService = daemon().service(ClientService.class);
        final IndexDbService indexDbService = daemon().service(IndexDbService.class);

        final List<Project> projects = projectService.getProjects();
        final FileIndexServiceFactory factory = daemon().service(FileIndexServiceFactory.class);
        final User user = daemon().getUser();
        final ActorRef fileChangeActor = daemon().getFileChangeActor();

        for (final Project project : projects) {
            factory.create(project, fileChangeActor);
        }
        logger.info("FileIndex Plugin started!");
    }

	@Override
	public void onStop() {
	
	}
}
