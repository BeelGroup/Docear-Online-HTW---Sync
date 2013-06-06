package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.UploadResponse;
import org.docear.syncdaemon.fileindex.messages.LocalFileChanged;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.messages.FileChangeEvent;
import org.docear.syncdaemon.messages.FileConflictEvent;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class ServerSynchronisationActor extends UntypedActor {
	private static final Logger logger = LoggerFactory.getLogger(ServerSynchronisationActor.class);

    private final ClientService clientService;
    private final IndexDbService indexDbService;

    public ServerSynchronisationActor(ClientService clientService, IndexDbService indexDbService) {
        this.clientService = clientService;
        this.indexDbService = indexDbService;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof FileChangeEvent) {
            //TODO with dispatcher
            final FileMetaData fileMetaData = ((LocalFileChanged) message).getFileMetaData();
            final Project project = ((LocalFileChanged) message).getProject();
            
            //TODO user credentials required to use clientService
            final UploadResponse uploadResponse = null; //clientService.upload(project, fileMetaData);
            if (uploadResponse.hasConflicts()) {
                //TODO rename conflicted file
                //TODO suppress jNotify events for download
            	//TODO user credentials required to use clientService
                //clientService.download(uploadResponse.getCurrentServerMetaData());
                indexDbService.save(uploadResponse.getConflictedServerMetaData());
            }
            indexDbService.save(uploadResponse.getCurrentServerMetaData());
        } else if (message instanceof FileConflictEvent){
        	logger.info(((FileConflictEvent)message).toString());
        }
    }
}
