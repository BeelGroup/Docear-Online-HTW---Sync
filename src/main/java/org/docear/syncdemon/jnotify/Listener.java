package org.docear.syncdemon.jnotify;

import akka.actor.ActorRef;
import net.contentobjects.jnotify.JNotifyListener;
import org.docear.syncdemon.messages.FileCreatedMessage;
import org.joda.time.DateTime;

import java.io.File;

public class Listener implements JNotifyListener {

    final ActorRef recipient;

    public Listener(ActorRef recipient) {
        this.recipient = recipient;
    }

    @Override
    public void fileCreated(final int wd, final String rootPath, final String name) {
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
        });
    }

    @Override
    public void fileDeleted(final int wd, final String rootPath, final String name) {
    }

    @Override
    public void fileModified(final int wd, final String rootPath, final String name) {
    }

    @Override
    public void fileRenamed(final int wd, final String rootPath, final String oldName, final String newName) {
    }
}
