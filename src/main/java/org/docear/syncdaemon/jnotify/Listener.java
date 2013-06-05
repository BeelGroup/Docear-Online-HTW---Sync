package org.docear.syncdaemon.jnotify;

import net.contentobjects.jnotify.JNotifyListener;

import org.docear.syncdaemon.actors.Service;
import org.docear.syncdaemon.messages.FileChangeEvent;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

public class Listener extends Service implements JNotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(Project project, ActorRef recipient) {
        super(recipient, project);
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
}
