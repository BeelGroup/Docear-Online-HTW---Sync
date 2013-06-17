package org.docear.syncdaemon.fileindex;

import java.util.List;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.docear.syncdaemon.config.ConfigService;
import org.docear.syncdaemon.projects.Project;

import akka.actor.ActorRef;

public class FileIndexPlugin extends Plugin{

	public FileIndexPlugin(Daemon daemon) {
		super(daemon);
	}

	@Override
	public void onStart() {
        final ConfigService configService = daemon().service(ConfigService.class);
        final List<Project> projects = configService.getProjects();
        final FileIndexServiceFactory factory = daemon().service(FileIndexServiceFactory.class);
        final ActorRef fileChangeActor = daemon().getFileChangeActor();
        for (final Project project : projects) {
            factory.create(project, fileChangeActor);
        }
    }

	@Override
	public void onStop() {
	
	}
}
