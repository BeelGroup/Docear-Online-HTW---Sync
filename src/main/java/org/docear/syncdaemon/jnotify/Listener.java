package org.docear.syncdaemon.jnotify;

import akka.actor.ActorRef;
import net.contentobjects.jnotify.JNotifyListener;

import org.docear.syncdaemon.messages.FileChangeEvent;
import org.docear.syncdaemon.projects.Project;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Listener implements JNotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    private final Project project;
    final ActorRef recipient;

    public Listener(Project project, ActorRef recipient) {
        this.project = project;
        this.recipient = recipient;
    }

    @Override
    public void fileCreated(final int wd, final String rootPath, final String name) {
        logger.debug("fileCreated {}/{}", rootPath, name);
        sendMessage(rootPath, name);
    }

    @Override
    public void fileDeleted(final int wd, final String rootPath, final String name) {
        logger.debug("fileDeleted {}/{}", rootPath, name);
        sendMessage(rootPath, name);
    }

    @Override
    public void fileModified(final int wd, final String rootPath, final String name) {
        logger.debug("fileModified {}/{}", rootPath, name);
        sendMessage(rootPath, name);
    }

    @Override
    public void fileRenamed(final int wd, final String rootPath, final String oldName, final String newName) {
        logger.debug("fileRenamed rootpath={}, oldName={}, newName={}", rootPath, oldName, newName);
        sendMessage(rootPath, oldName);
        sendMessage(rootPath, newName);
    }

    private void sendMessage(final String rootPath, final String name) {
        final String projectRelativePath = Project.toProjectRelativePath(rootPath, name);
        final FileChangeEvent message = new FileChangeEvent(projectRelativePath, project.getId());
        recipient.tell(message, recipient);
    }
}
