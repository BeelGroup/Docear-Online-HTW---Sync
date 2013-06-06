package org.docear.syncdaemon.jnotify;

import net.contentobjects.jnotify.JNotifyListener;

import org.apache.commons.io.FilenameUtils;
import org.docear.syncdaemon.messages.FileChangeEvent;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

public class Listener implements JNotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

	private final ActorRef recipient;
	private final Project project;
    
    public Listener(Project project, ActorRef recipient) {
		this.recipient = recipient;
		this.project = project;
    }

    @Override
    public void fileCreated(final int wd, final String rootPath, final String name) {
        logger.debug("fileCreated {}/{}", rootPath, name);
        sendFileChangedMessage(rootPath, name);
    }

    @Override
    public void fileDeleted(final int wd, final String rootPath, final String name) {
        logger.debug("fileDeleted {}/{}", rootPath, name);
        sendFileChangedMessage(rootPath, name);
    }

    @Override
    public void fileModified(final int wd, final String rootPath, final String name) {
        logger.debug("fileModified {}/{}", rootPath, name);
        sendFileChangedMessage(rootPath, name);
    }

    @Override
    public void fileRenamed(final int wd, final String rootPath, final String oldName, final String newName) {
        logger.debug("fileRenamed rootpath={}, oldName={}, newName={}", rootPath, oldName, newName);
        sendFileChangedMessage(rootPath, oldName);
        sendFileChangedMessage(rootPath, newName);
    }
    
	private void sendFileChangedMessage(final String path, final String name) {
		String absolutePath = FilenameUtils.concat(path, name);
		final FileChangeEvent message = new FileChangeEvent(project.toRelativePath(absolutePath), project.getId());
        recipient.tell(message, recipient);
	}
}
