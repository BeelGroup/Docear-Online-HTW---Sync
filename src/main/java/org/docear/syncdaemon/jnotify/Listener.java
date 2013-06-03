package org.docear.syncdaemon.jnotify;

import akka.actor.ActorRef;
import net.contentobjects.jnotify.JNotifyListener;

import org.docear.syncdaemon.messages.FileCreatedMessage;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Listener implements JNotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    final ActorRef recipient;

    public Listener(ActorRef recipient) {
        this.recipient = recipient;
    }

    @Override
    public void fileCreated(final int wd, final String rootPath, final String name) {
        logger.debug("fileCreated {}/{}", rootPath, name);
        recipient.tell(new FileCreatedMessage() {
            final DateTime timestamp = DateTime.now();
            final String path = rootPath + File.separator + name;

            @Override
            public String getAbsolutePath() {
                return path;
            }

            @Override
            public File getFile() {
                return new File(getAbsolutePath());
            }

            @Override
            public DateTime getTimestamp() {
                return timestamp;
            }
        }, recipient);
    }

    @Override
    public void fileDeleted(final int wd, final String rootPath, final String name) {
        logger.debug("fileDeleted {}/{}", rootPath, name);
    }

    @Override
    public void fileModified(final int wd, final String rootPath, final String name) {
        logger.debug("fileModified {}/{}", rootPath, name);
    }

    @Override
    public void fileRenamed(final int wd, final String rootPath, final String oldName, final String newName) {
        logger.debug("fileRenamed rootpath={}, oldName={}, newName={}", rootPath, oldName, newName);
    }
}
