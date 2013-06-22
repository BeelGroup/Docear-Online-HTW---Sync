package org.docear.syncdaemon.fileindex;

import java.util.List;

import org.docear.syncdaemon.fileactors.Messages.FileChangedLocally;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.PersistenceException;
import org.docear.syncdaemon.indexdb.h2.H2IndexDbService;
import org.docear.syncdaemon.projects.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class FileIndexServiceImpl extends UntypedActor implements
        FileIndexService {
    private static final Logger logger = LoggerFactory.getLogger(FileIndexServiceImpl.class);

    private ActorRef recipient;
    private Project project;
    private final IndexDbService indexDbService;

    public FileIndexServiceImpl() {
        indexDbService = new H2IndexDbService();
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof StartScanMessage) {
            this.project = ((StartScanMessage) message).getProject();
            this.recipient = ((StartScanMessage) message).getFileChangeActor();
            scanProject();
        }
    }

    @Override
    public void scanProject() {
        try {
            long localRev = indexDbService.getProjectRevision(project.getId());

            if (localRev != project.getRevision()) {
                List<FileMetaData> fmdsFromScan = FileReceiver.receiveFiles(project);
                List<FileMetaData> fmdsFromIndexDb = indexDbService.getFileMetaDatas(project.getId());
                
                // TODO user credentials required to use clientService
                for (FileMetaData fmdFromScan : fmdsFromScan) {
                	FileMetaData match = null;
                	for (FileMetaData fmdFromIndexDb: fmdsFromIndexDb){
                		if (fmdFromScan.getPath().equals(fmdFromIndexDb.getPath())){
                			match = fmdFromIndexDb;
                			break;
                		}
                	}
                
                	if (match == null || fmdFromScan.isChanged(match)) {
                		sendFileChangedMessage(fmdFromScan);
                	}
                	
                	if (match != null){
                		fmdsFromIndexDb.remove(match);
                    }
                }
                
                for (FileMetaData fmdFromIndexDb : fmdsFromIndexDb) {
                	sendFileChangedMessage(fmdFromIndexDb);
                }
            }
        } catch (PersistenceException e) {
            logger.error("can't scan projects", e);
        }
    }

    private void sendFileChangedMessage(final FileMetaData fmd) {
    	final FileChangedLocally message = new FileChangedLocally(this.project, fmd);
        recipient.tell(message, recipient);
    }

}