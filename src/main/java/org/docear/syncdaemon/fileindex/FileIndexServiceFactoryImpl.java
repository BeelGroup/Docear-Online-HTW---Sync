package org.docear.syncdaemon.fileindex;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.docear.syncdaemon.projects.Project;

public class FileIndexServiceFactoryImpl implements FileIndexServiceFactory {

    private ActorSystem system;

    public FileIndexServiceFactoryImpl() {
        system = ActorSystem.create("FileIndexServiceFactoryActorSystem");
    }

    @Override
    public void create(Project project, ActorRef fileChangeActor) {
        ActorRef fileIndexService = system.actorOf(new Props(FileIndexServiceImpl.class));
        fileIndexService.tell(new StartScanMessage(project, fileChangeActor));
    }
}
