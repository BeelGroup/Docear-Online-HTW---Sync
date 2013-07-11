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

    private static final Logger logger = LoggerFactory.getLogger(ListenForUpdatesActor.class);
    private final ConfigService configService;
    private User user;
    private ClientService clientService;
    private IndexDbService indexDbService;
    private ActorRef fileChangeActor;
    private ActorSystem system = ActorSystem.apply();


    public ListenForUpdatesActor(User user, ClientService clientService, ActorRef fileChangeActor, IndexDbService indexDb, ConfigService configService) {
        this.user = user;
        this.clientService = clientService;
        this.configService = configService;
        this.indexDbService = indexDb;
        this.fileChangeActor = fileChangeActor;
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Messages.StartListening) {
            logger.debug("Start listening for projects");
            Messages.StartListening startListing = (Messages.StartListening) message;
            Map<String, Long> projectIdRevisionMap = startListing.getProjectIdRevisionMap();
            if (projectIdRevisionMap == null) {
                projectIdRevisionMap = new HashMap<String, Long>();
            }
            projectIdRevisionMap.putAll(indexDbService.getProjects());
            clientService.listenForUpdates(user, projectIdRevisionMap, this.getSelf());
        } else if (message instanceof ListenForUpdatesResponse) {
            ListenForUpdatesResponse response = (ListenForUpdatesResponse) message;
            final Map<String, Long> updatedProjects = response.getUpdatedProjects();
            final Map<String, Long> newProjects = response.getNewProjects();
            final List<String> deletedProjects = response.getDeletedProjects();

            logger.debug("ListenForUpdates => updated: " + updatedProjects.size() + "; newProjects: " + newProjects.size() + "; deletedProjects: " + deletedProjects.size());

            // send project updates to filechangeactor

            if (updatedProjects != null && updatedProjects.size() > 0) {
                for (Entry<String, Long> entry : updatedProjects.entrySet()) {
                    Project localProject = getProject(entry.getKey());
                    DeltaResponse delta = clientService.delta(user, localProject.getId(), localProject.getRevision());
                    List<FileMetaData> fmds = delta.getServerMetaDatas();

                    for (FileMetaData fmd : fmds) {
                        logger.debug("Updated file path=" + fmd.getPath() + " hash=" + fmd.getHash());
                        FileChangedOnServer changeMessage = new FileChangedOnServer(localProject, fmd);
                        fileChangeActor.tell(changeMessage, this.getSelf());
                    }

                    indexDbService.setProjectRevision(entry.getKey(), entry.getValue());
                }

            }

            // send new projects to filechangeactor
            if (newProjects != null && newProjects.size() > 0) {
                for (Entry<String, Long> entry : newProjects.entrySet()) {
                    final String projectId = entry.getKey();
                    logger.debug("New Project: " + projectId);
                    final Project onlineProject = clientService.getProject(user, projectId);
                    final String rootPath = configService.getSyncDaemonHome().toString() + "/" + onlineProject.getName() + "_" + projectId;
                    final Project localProject = new Project(projectId, rootPath, 0L, onlineProject.getName());

                    // tell fileChangeActor that there is a new project to create init folder and index db entrys
                    fileChangeActor.tell(new ProjectAdded(localProject), this.getSelf());

                    // get deltas and process them as usual
                    DeltaResponse delta = clientService.delta(user, localProject.getId(), localProject.getRevision());
                    List<FileMetaData> fmds = delta.getServerMetaDatas();

                    for (FileMetaData fmd : fmds) {
                        logger.debug("New file in new project path=" + fmd.getPath() + " hash=" + fmd.getHash());
                        FileChangedOnServer changeMessage = new FileChangedOnServer(localProject, fmd);
                        fileChangeActor.tell(changeMessage, this.getSelf());
                    }

                    // add project to projectIdRevisonMap for next iteration
                    this.indexDbService.setProjectRevision(localProject.getId(), entry.getValue());
                    this.configService.addProject(localProject);
                    this.configService.saveConfig();
                }
            }

            // send deleted projects to filechangeactor

            if (deletedProjects != null) {
                for (String projectId : deletedProjects) {
                    logger.debug("Deleted Project: " + projectId);
                    final Project localProject = getProject(projectId);

                    // tell fileChangeActor that there is a deleted project
                    fileChangeActor.tell(new ProjectDeleted(localProject), this.getSelf());

                    // remove project from projectIdRevisonMap for next iteration
                    this.indexDbService.deleteProject(projectId);
                    this.configService.deleteProject(localProject);
                    this.configService.saveConfig();
                }
            }

            // schedule re-listening
            this.getSelf().tell(new Messages.ListenAgain(), this.getSelf());
        } else if (message instanceof Messages.ListenAgain) {
            logger.debug("Listening again for projectId " + indexDbService.getProjects().toString());
            try {
                ListenForUpdatesResponse restartResponse = clientService.listenForUpdates(user, indexDbService.getProjects(), null);
                if (restartResponse != null) {
                    this.getSelf().tell(restartResponse, this.getSelf());
                } else {
                    logger.error("problem with client service. Retry in 10 seconds. ");
                    system.scheduler().scheduleOnce(Duration.create(10, TimeUnit.SECONDS), this.getSelf(), new Messages.ListenAgain(), system.dispatcher());
                }
            } catch (Exception e) {
                logger.error("problem with client service. Retry in 10 seconds. ", e);
                system.scheduler().scheduleOnce(Duration.create(10, TimeUnit.SECONDS), this.getSelf(), new Messages.ListenAgain(), system.dispatcher());
            }


        }
    }

    private Project getProject(String projectId) throws PersistenceException {
        final Project confProj = configService.getProject(projectId);
        final Project mergedProj = new Project(projectId, confProj.getRootPath(), indexDbService.getProjectRevision(projectId), confProj.getName());
        return mergedProj;
    }
}

