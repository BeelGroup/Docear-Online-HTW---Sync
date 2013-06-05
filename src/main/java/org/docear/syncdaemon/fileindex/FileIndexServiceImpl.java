package org.docear.syncdaemon.fileindex;

import java.util.List;

import org.docear.syncdaemon.actors.Service;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ClientServiceImpl;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.IndexDbServiceImpl;
import org.docear.syncdaemon.projects.Project;

import akka.actor.ActorRef;

public class FileIndexServiceImpl extends Service implements FileIndexService {

	private final ClientService clientService;
	private final IndexDbService indexDbService;
	
	public FileIndexServiceImpl(ActorRef recipient, Project project){
		super(recipient, project);
		clientService = new ClientServiceImpl();
		indexDbService = new IndexDbServiceImpl();
	}
	
	@Override
	public void scanProject() {
		long localRev = indexDbService.getProjectRevision(project.getId());
		
		if (localRev != project.getRevision()){
			List<FileMetaData> files = FileReceiver.receiveFiles(project);
			
			// TODO user credentials required to use clientService
			for (FileMetaData fmdFromScan : files){			
		        final FileMetaData fmdFromIndexDb = indexDbService.getFileMetaData(fmdFromScan);
		        if (fmdFromScan.isChanged(fmdFromIndexDb)) {
		        	sendConflictMesage(fmdFromScan);
		        } else {
		        	sendFileChangedMessage(fmdFromScan);
		        }
			}
		}
	}

}
