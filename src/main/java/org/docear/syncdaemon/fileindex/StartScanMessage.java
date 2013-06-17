package org.docear.syncdaemon.fileindex;

import java.io.Serializable;

import org.docear.syncdaemon.projects.Project;

import akka.actor.ActorRef;

public class StartScanMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private Project project;
	private ActorRef fileChangeActor;
	
	public StartScanMessage(Project project, ActorRef fileChangeActor){
		this.project = project;
		this.fileChangeActor = fileChangeActor;
	}

	public Project getProject() {
		return project;
	}

	public ActorRef getFileChangeActor() {
		return fileChangeActor;
	}
}
