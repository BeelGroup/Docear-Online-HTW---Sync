package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.projects.ProjectService;

import java.util.List;

public class FileIndexPlugin extends Plugin{

	public FileIndexPlugin(Daemon daemon) {
		super(daemon);
	}

	@Override
	public void onStart() {
        final ProjectService projectService = daemon().service(ProjectService.class);
        final List<Project> projects = projectService.getProjects();
        final FileIndexServiceFactory factory = daemon().service(FileIndexServiceFactory.class);
        for (final Project project : projects) {
            factory.create().scanProject(project);
        }
    }

	@Override
	public void onStop() {
	}
}
