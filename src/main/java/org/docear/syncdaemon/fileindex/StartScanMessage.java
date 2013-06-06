package org.docear.syncdaemon.fileindex;

import java.io.Serializable;

import org.docear.syncdaemon.projects.Project;

import akka.actor.ActorRef;

public class StartScanMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private Project project;
	private ActorRef serverSynchronisationActor;
	
	public StartScanMessage(Project project, ActorRef serverSynchronisationActor){
		this.project = project;
		this.serverSynchronisationActor = serverSynchronisationActor;
	}

	public Project getProject() {
		return project;
	}

	public ActorRef getServerSynchronisationActor() {
		return serverSynchronisationActor;
	}
}
