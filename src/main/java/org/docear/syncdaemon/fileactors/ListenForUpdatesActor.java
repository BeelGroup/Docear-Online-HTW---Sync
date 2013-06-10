package org.docear.syncdaemon.fileactors;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ListenForUpdatesResponse;
import org.docear.syncdaemon.users.User;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class ListenForUpdatesActor extends UntypedActor {

    private User user;
    private ClientService clientService;
    private ActorRef fileChangeActor;

    private Map<String, Long> projectIdRevisonMap;

    public ListenForUpdatesActor(User user, ClientService clientService, ActorRef fileChangeActor) {
        this.user = user;
        this.clientService = clientService;
        this.fileChangeActor = fileChangeActor;
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Messages.StartListening) {
        	clientService.listenForUpdates(user, projectIdRevisionMap, this.getSelf());
        } else if(message instanceof ListenForUpdatesResponse) {
        	ListenForUpdatesResponse response = (ListenForUpdatesResponse)message;
        	
        	// send project updates to filechangeactor
        	Map<String, Long> updatedProjects = response.getUpdatedProjects();
        	for (Entry<String, Long> entry : updatedProjects.entrySet()){
        		Messages.ProjectUpdated update = new Messages.ProjectUpdated(entry.getKey(), entry.getValue());
        		fileChangeActor.tell(update, this.getSelf());
        	}

        	// send new projects to filechangeactor
        	Map<String, Long> newProjects = response.getNewProjects();
        	for (Entry<String, Long> entry : newProjects.entrySet()){
        		Messages.ProjectAdded add = new Messages.ProjectAdded(entry.getKey(), entry.getValue());
        		fileChangeActor.tell(add, this.getSelf());
        	}
        	
        	// send deleted projects to filechangeactor
        	List<String> deletedProjects = response.getDeletedProjects();
        	for (String projectId : deletedProjects){
        		Messages.ProjectDeleted delete = new Messages.ProjectDeleted(projectId);
        		fileChangeActor.tell(delete, this.getSelf());
        	}
        	
        	// listen again
        	this.getSelf().tell("listen", this.getSelf());
        } else if(message.equals("listen")) {
            //TODO listen again
        }
    }
}
