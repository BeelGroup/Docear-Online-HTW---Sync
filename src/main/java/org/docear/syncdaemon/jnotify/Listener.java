package org.docear.syncdaemon.jnotify;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import net.contentobjects.jnotify.JNotifyListener;
import org.docear.syncdaemon.fileactors.Messages.FileChangedLocally;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Listener implements JNotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);
    private final ActorRef recipient;
    private final Project project;
    private final HashAlgorithm hashAlgorithm;
    private final ActorSystem system;
    private final Map<String,Cancellable> pathJobMap = new HashMap<String, Cancellable>();

    public Listener(Project project, ActorRef recipient) {
        this.recipient = recipient;
        this.system = ActorSystem.apply();

        this.project = project;
        this.hashAlgorithm = new SHA2();
    }

    @Override
    public void fileCreated(final int wd, final String rootPath, final String name) {
        logger.debug("fileCreated {}/{}", rootPath, name);
        scheduleChange(rootPath,name,false);

    }

    @Override
    public void fileDeleted(final int wd, final String rootPath, final String name) {
        logger.debug("fileDeleted {}/{}", rootPath, name);
        scheduleChange(rootPath,name,true);
    }

    @Override
    public void fileModified(final int wd, final String rootPath, final String name) {
        logger.debug("fileModified {}/{}", rootPath, name);
        scheduleChange(rootPath,name,false);
    }

    @Override
    public void fileRenamed(final int wd, final String rootPath, final String oldName, final String newName) {
        logger.debug("fileRenamed rootpath={}, oldName={}, newName={}", rootPath, oldName, newName);
        scheduleChange(rootPath,oldName,true);
        scheduleChange(rootPath,newName,false);
    }

    private void scheduleChange(String rootPath, String filename, boolean isDeleted) {
        final SendChangeRunnable sendChangeRunnable = new SendChangeRunnable(project,recipient,hashAlgorithm,rootPath,filename,isDeleted);
        final Cancellable cancellable = system.scheduler().scheduleOnce(Duration.apply(2, TimeUnit.SECONDS), sendChangeRunnable,system.dispatcher());
        putInCancellableMap(rootPath,filename,cancellable);
    }

    public void putInCancellableMap(String rootPath, String path, Cancellable cancellable) {
        final String fullQualifier = rootPath+path;
        if(pathJobMap.containsKey(fullQualifier)) {
            final Cancellable oldCancellable = pathJobMap.get(fullQualifier);
            if(!oldCancellable.isCancelled())
                oldCancellable.cancel();
        }
        pathJobMap.put(fullQualifier,cancellable);
    }

//    private void sleep(long millis) {
//        try {
//            Thread.sleep(millis);
//        } catch (InterruptedException e) {
//        }
//    }



//    private void sendFileChangedMessage(final FileMetaData fileMetaData) {
//        final FileChangedLocally message = new FileChangedLocally(this.project, fileMetaData);
//
//        recipient.tell(message, recipient);
//    }


    private static class SendChangeRunnable implements Runnable {
        private final Project project;
        private final ActorRef recipient;
        private final HashAlgorithm hashAlgorithm;
        private final String rootPath;
        private final String filename;
        private final boolean isDeleted;

        private SendChangeRunnable(Project project, ActorRef recipient, HashAlgorithm hashAlgorithm, String rootPath, String filename, boolean deleted) {
            this.project = project;
            this.recipient = recipient;
            this.hashAlgorithm = hashAlgorithm;
            this.rootPath = rootPath;
            this.filename = filename;
            isDeleted = deleted;
        }


        @Override
        public void run() {

            final FileMetaData fileMetaData = FileMetaData.fromFS(hashAlgorithm,project.getId(),rootPath,filename,isDeleted);
            logger.debug("scr => getting meta data: "+fileMetaData);
            final FileChangedLocally message = new FileChangedLocally(this.project, fileMetaData);
            logger.debug("scr => sending file change to recipient. Filename: "+filename);
            recipient.tell(message, recipient);
        }

    }
}
