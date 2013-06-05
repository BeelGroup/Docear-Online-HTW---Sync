package org.docear.syncdaemon.actors;

import org.apache.commons.io.FilenameUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.messages.FileChangeEvent;
import org.docear.syncdaemon.projects.Project;

import akka.actor.ActorRef;

public abstract class Service {

	private final ActorRef recipient;
	protected final Project project;
	
	public Service(ActorRef recipient, Project project){
		this.recipient = recipient;
		this.project = project;
	}
	
	protected void sendMessage(final String path, final String name) {
		String absolutePath = FilenameUtils.concat(path, name);
		final FileChangeEvent message = new FileChangeEvent(project.toRelativePath(absolutePath), project.getId());
        recipient.tell(message, recipient);
	}
	
	protected void sendMessage(final FileMetaData fmd) {
        final FileChangeEvent message = new FileChangeEvent(fmd.getPath(), project.getId());
        recipient.tell(message, recipient);
    }
}
