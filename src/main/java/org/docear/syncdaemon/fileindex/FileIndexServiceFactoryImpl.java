package org.docear.syncdaemon.fileindex;

import akka.actor.*;
import akka.actor.Actor;
import akka.actor.Props;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.projects.Project;

public class FileIndexServiceFactoryImpl implements FileIndexServiceFactory {

    private IndexDbService indexDbService;
    private ActorSystem system;

    public FileIndexServiceFactoryImpl() {

    }

    @Override
    public void setActorSystem(ActorSystem system) {
        this.system = system;
    }

    @Override
    public void setIndexDbService(IndexDbService indexDbService) {
        this.indexDbService = indexDbService;
    }

    @Override
    public void create(Project project, ActorRef fileChangeActor) {
        ActorRef fileIndexService = system.actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return (Actor) new FileIndexServiceImpl(indexDbService);
            }
        }));
        fileIndexService.tell(new StartScanMessage(project, fileChangeActor), null);
    }
}
