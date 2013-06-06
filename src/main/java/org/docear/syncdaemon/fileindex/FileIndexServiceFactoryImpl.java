package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.projects.Project;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class FileIndexServiceFactoryImpl implements FileIndexServiceFactory{

	private ActorSystem system;
	
	public FileIndexServiceFactoryImpl(){
		 system = ActorSystem.create("FileIndexServiceFactoryActorSystem");
	}
	
	@Override
	public void create(Project project, ActorRef serverSynchronisationActor) {
		ActorRef fileIndexService = system.actorOf(new Props(FileIndexServiceImpl.class));
		fileIndexService.tell(new StartScanMessage(project, serverSynchronisationActor));
	}
}
