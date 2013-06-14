package org.docear.syncdaemon.fileactors;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.DeltaResponse;
import org.docear.syncdaemon.client.ListenForUpdatesResponse;
import org.docear.syncdaemon.fileactors.Messages.FileChangedOnServer;
import org.docear.syncdaemon.fileactors.Messages.ProjectAdded;
import org.docear.syncdaemon.fileactors.Messages.ProjectDeleted;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class ListenForUpdatesActor extends UntypedActor {

    private User user;
    private ClientService clientService;
    private IndexDbService indexDbService;
    private ActorRef fileChangeActor;

    private Map<String, Long> projectIdRevisonMap;

    public ListenForUpdatesActor(User user, ClientService clientService, ActorRef fileChangeActor, IndexDbService indexDb) {
        this.user = user;
        this.clientService = clientService;
        this.indexDbService = indexDbService;
        this.fileChangeActor = fileChangeActor;
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Messages.StartListening) {
        	clientService.listenForUpdates(user, this.projectIdRevisonMap, this.getSelf());
        } else if(message instanceof ListenForUpdatesResponse) {
        	ListenForUpdatesResponse response = (ListenForUpdatesResponse)message;
        	
        	// send project updates to filechangeactor
        	Map<String, Long> updatedProjects = response.getUpdatedProjects();
        	for (Entry<String, Long> entry : updatedProjects.entrySet()){
        		Project localProject = new Project(entry.getKey(),
        				indexDbService.getProjectRootPath(entry.getKey()),
        				indexDbService.getProjectRevision(entry.getKey()));
        		DeltaResponse delta = clientService.delta(user, localProject.getId(), localProject.getRevision());
        		List<FileMetaData> fmds = delta.getServerMetaDatas();
        		
        		for (FileMetaData fmd : fmds){
        			FileChangedOnServer changeMessage = new FileChangedOnServer(localProject, fmd);
        			fileChangeActor.tell(changeMessage, this.getSelf());
        		}
        	}

        	// send new projects to filechangeactor
        	Map<String, Long> newProjects = response.getNewProjects();
        	for (Entry<String, Long> entry : newProjects.entrySet()){
        		Project localProject = new Project(entry.getKey(),
        				null, // TODO replace with something like daemon().getDefaultProjectPath(),
        				0);
        		
        		// tell fileChangeActor that there is a new project to create init folder and index db entrys
        		fileChangeActor.tell(new ProjectAdded(localProject), this.getSelf());
        		
        		// get deltas and process them as usual
        		DeltaResponse delta = clientService.delta(user, localProject.getId(), localProject.getRevision());
        		List<FileMetaData> fmds = delta.getServerMetaDatas();
        		
        		for (FileMetaData fmd : fmds){
        			FileChangedOnServer changeMessage = new FileChangedOnServer(localProject, fmd);
        			fileChangeActor.tell(changeMessage, this.getSelf());
        		}
        		
        		// add project to projectIdRevisonMap for next iteration
        		this.projectIdRevisonMap.put(localProject.getId(), entry.getValue());
        	}
        	
        	// send deleted projects to filechangeactor
        	List<String> deletedProjects = response.getDeletedProjects();
        	for (String projectId : deletedProjects){
        		Project localProject = new Project(projectId,
        				indexDbService.getProjectRootPath(projectId),
        				indexDbService.getProjectRevision(projectId));
        		
        		// tell fileChangeActor that there is a deleted project
        		fileChangeActor.tell(new ProjectDeleted(localProject), this.getSelf());
        		
        		// remove project from projectIdRevisonMap for next iteration        		
        		if (projectIdRevisonMap.containsKey(projectId)){
        			this.projectIdRevisonMap.remove(projectId);
        		}
        	}
        	
        	// listen again
        	this.getSelf().tell(new Messages.StartListening(this.projectIdRevisonMap), this.getSelf());
        }
    }
}
