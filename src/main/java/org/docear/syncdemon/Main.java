package org.docear.syncdemon;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import net.contentobjects.jnotify.JNotify;
import org.docear.syncdemon.actors.LogActor;
import org.docear.syncdemon.actors.SyncDispatchActor;
import org.docear.syncdemon.jnotify.Listener;
import org.docear.syncdemon.jnotify.NativeLibraryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FileUtils.getUserDirectory;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0) {
            final String folderToWatch = args[0];
            start(folderToWatch);
        } else {
            System.err.println("Cannot start. First command line param is folder to watch.");
        }
    }

    private static void start(String folderToWatch) throws IOException, InterruptedException {
        final Logger logger = LoggerFactory.getLogger(Main.class);
        final ActorRef dispatcher = initializeDispatchActor();
        initializeJnotifyNativeLibraries();
        try {
            int mask = JNotify.FILE_CREATED |
                    JNotify.FILE_DELETED |
                    JNotify.FILE_MODIFIED |
                    JNotify.FILE_RENAMED;
            boolean watchSubtree = true;
            forceMkdir(new File(folderToWatch));
            int watchID = JNotify.addWatch(folderToWatch, mask, watchSubtree, new Listener(dispatcher));
            logger.info("watching " + folderToWatch);
            Thread.sleep(5000000);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("libjnotify.so or dll must be in java.library.path");
            throw e;
        }
    }

    private static void initializeJnotifyNativeLibraries() throws IOException {
        final File destinationFolderNativeLibrary = new File(getUserDirectory(), ".docear-sync-demon" + File.separator + "native-libraries");
        new NativeLibraryResolver(destinationFolderNativeLibrary).run();
    }

    private static ActorRef initializeDispatchActor() {
        final ActorSystem system = ActorSystem.create("syncDemon");
        final ActorRef logActor = system.actorOf(new Props(LogActor.class), "logger");
        final ActorRef syncDispatchActor = system.actorOf(new Props(SyncDispatchActor.class), "syncDispatcher");
        return syncDispatchActor;
    }
}
