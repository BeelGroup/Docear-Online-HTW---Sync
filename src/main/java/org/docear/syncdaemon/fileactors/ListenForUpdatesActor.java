package org.docear.syncdaemon.fileactors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.DeltaResponse;
import org.docear.syncdaemon.client.ListenForUpdatesResponse;
import org.docear.syncdaemon.config.ConfigService;
import org.docear.syncdaemon.fileactors.Messages.FileChangedOnServer;
import org.docear.syncdaemon.fileactors.Messages.ProjectAdded;
import org.docear.syncdaemon.fileactors.Messages.ProjectDeleted;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.PersistenceException;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class ListenForUpdatesActor extends UntypedActor {

    private User user;
    private ClientService clientService;
    private final ConfigService configService;
    private IndexDbService indexDbService;
    private ActorRef fileChangeActor;
    
    private static final Logger logger = LoggerFactory.getLogger(ListenForUpdatesActor.class);

    private Map<String, Long> projectIdRevisonMap;

    public ListenForUpdatesActor(User user, ClientService clientService, ActorRef fileChangeActor, IndexDbService indexDb, ConfigService configService) {
        this.user = user;
        this.clientService = clientService;
        this.configService = configService;
        this.indexDbService = indexDb;
        this.fileChangeActor = fileChangeActor;
        this.projectIdRevisonMap = new HashMap<String, Long>();
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Messages.StartListening) {
        	Messages.StartListening startListing = (Messages.StartListening)message;
        	this.projectIdRevisonMap = startListing.getProjectIdRevisionMap();
        	if (this.projectIdRevisonMap == null){
        		this.projectIdRevisonMap = new HashMap<String, Long>();
        	}
            clientService.listenForUpdates(user, this.projectIdRevisonMap, this.getSelf());
        	//this.getSelf().tell(), this.getSelf());
        } else if(message instanceof ListenForUpdatesResponse) {
        	ListenForUpdatesResponse response = (ListenForUpdatesResponse)message;  
        	
        	// send project updates to filechangeactor
        	Map<String, Long> updatedProjects = response.getUpdatedProjects();
        	if (updatedProjects != null && updatedProjects.size() > 0){
	        	for (Entry<String, Long> entry : updatedProjects.entrySet()){
	        		Project localProject = getProject(entry.getKey());//TODO rootpath
	        		DeltaResponse delta = clientService.delta(user, localProject.getId(), localProject.getRevision());
	        		List<FileMetaData> fmds = delta.getServerMetaDatas();
	        		
	        		for (FileMetaData fmd : fmds){
	        			FileChangedOnServer changeMessage = new FileChangedOnServer(localProject, fmd);
	        			fileChangeActor.tell(changeMessage, this.getSelf());
	        		}

                    indexDbService.setProjectRevision(entry.getKey(),entry.getValue());
                    projectIdRevisonMap.put(entry.getKey(),entry.getValue());
	        	}

        	}

        	// send new projects to filechangeactor
        	Map<String, Long> newProjects = response.getNewProjects();
        	if (newProjects != null && newProjects.size() > 0){
	        	for (Entry<String, Long> entry : newProjects.entrySet()){
	        		logger.debug("New Project: " + entry.getKey());
	        		Project localProject = new Project(entry.getKey(),
	        				configService.getSyncDaemonHome().toString()+"/"+entry.getKey(),
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
        	}
        	
        	// send deleted projects to filechangeactor
        	List<String> deletedProjects = response.getDeletedProjects();
        	if (deletedProjects != null){
	        	for (String projectId : deletedProjects){
	        		logger.debug("Deleted Project: " + projectId);
	        		Project localProject = new Project(projectId,
	        				configService.getProjectRootPath(projectId),
	        				0L);
	        		
	        		// tell fileChangeActor that there is a deleted project
	        		fileChangeActor.tell(new ProjectDeleted(localProject), this.getSelf());
	        		
	        		// remove project from projectIdRevisonMap for next iteration        		
	        		if (projectIdRevisonMap.containsKey(projectId)){
	        			this.projectIdRevisonMap.remove(projectId);
	        		}
	        	}
        	}
	        	
        	// listen again
        	this.getSelf().tell(new Messages.ListenAgain(), this.getSelf());
        } else if (message instanceof Messages.ListenAgain){
        	ListenForUpdatesResponse restartResponse = clientService.listenForUpdates(user, this.projectIdRevisonMap, this.getSelf());
        	 
        	if (restartResponse != null) {
        		this.getSelf().tell(restartResponse, this.getSelf());
        	} else {
        		ActorSystem system = ActorSystem.apply();
        		system.scheduler().scheduleOnce(Duration.create(60, TimeUnit.SECONDS), this.getSelf(), new Messages.ListenAgain(), system.dispatcher());
        	}
        }
    }

    private Project getProject(String projectId) throws PersistenceException {
        return new Project(projectId, configService.getProjectRootPath(projectId), indexDbService.getProjectRevision(projectId));
    }
}

