package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.projects.Project;

import akka.actor.ActorRef;

public interface FileIndexServiceFactory {
    void create(Project project, ActorRef serverSynchronisationActor);
}
