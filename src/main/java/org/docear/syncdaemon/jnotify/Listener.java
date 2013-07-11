package org.docear.syncdaemon.jnotify;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import net.contentobjects.jnotify.JNotifyListener;
import org.docear.syncdaemon.fileactors.Messages.FileChangedLocally;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Listener implements JNotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);
    private final ActorRef recipient;
    private final Project project;
    private final ActorSystem system;
    private final Map<String,Cancellable> pathJobMap = new HashMap<String, Cancellable>();

    public Listener(Project project, ActorRef recipient) {
        this.recipient = recipient;
        this.system = ActorSystem.apply();

        this.project = project;
    }

    @Override
    public void fileCreated(final int wd, final String rootPath, final String name) {
        logger.debug("fileCreated {}/{}", rootPath, name);
        scheduleChange(rootPath,name);

    }

    @Override
    public void fileDeleted(final int wd, final String rootPath, final String name) {
        logger.debug("fileDeleted {}/{}", rootPath, name);
        scheduleChange(rootPath,name);
    }

    @Override
    public void fileModified(final int wd, final String rootPath, final String name) {
        logger.debug("fileModified {}/{}", rootPath, name);
        scheduleChange(rootPath,name);
    }

    @Override
    public void fileRenamed(final int wd, final String rootPath, final String oldName, final String newName) {
        logger.debug("fileRenamed rootpath={}, oldName={}, newName={}", rootPath, oldName, newName);
        scheduleChange(rootPath,oldName);
        scheduleChange(rootPath,newName);

        //when folder trigger rename for all sub files
        final File file = new File(rootPath,newName);
        if(file.isDirectory()) {
            final String prePath = newName.indexOf(File.separator) == -1 ? "" : newName.substring(0,newName.lastIndexOf(File.separator));
            final String oldPart = oldName.replace(prePath,"");
            final String newPart = newName.replace(prePath,"");
            folderRenameRecursion(rootPath,prePath,oldPart,newPart,"");
        }
    }

    private void folderRenameRecursion(final String rootPath, final String prePath, final String oldName, final String newName, final String postPath) {
        File folder = new File(rootPath,prePath+File.separator+newName);
        if(!postPath.isEmpty())
            folder = new File(folder,postPath);

        for(final File fileInFolder : folder.listFiles()) {
            final String namePart = fileInFolder.getName();
            final String newPostPath = postPath+File.separator+namePart;


            scheduleChange(rootPath,prePath+File.separator+oldName+newPostPath);
            scheduleChange(rootPath,prePath+File.separator+newName+newPostPath);

            if(fileInFolder.isDirectory())
                folderRenameRecursion(rootPath, prePath, oldName, newName, newPostPath);
        }
    }

    private void scheduleChange(String rootPath, String path) {
        final SendChangeRunnable sendChangeRunnable = new SendChangeRunnable(project,recipient,path);
        final Cancellable cancellable = system.scheduler().scheduleOnce(Duration.apply(500, TimeUnit.MILLISECONDS), sendChangeRunnable,system.dispatcher());
        putInCancellableMap(rootPath,path,cancellable);
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

    private static class SendChangeRunnable implements Runnable {
        private final Project project;
        private final ActorRef recipient;
        private final String path;

        private SendChangeRunnable(Project project, ActorRef recipient, String path) {
            this.project = project;
            this.recipient = recipient;
            this.path = path;
        }


        @Override
        public void run() {
            final FileChangedLocally message = new FileChangedLocally(this.project, path);
            logger.debug("scr => sending file change to recipient. Filename: "+ path);
            recipient.tell(message, recipient);
        }

    }
}
