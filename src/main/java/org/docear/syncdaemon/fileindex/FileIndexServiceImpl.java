package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ClientServiceImpl;
import org.docear.syncdaemon.client.FolderMetaData;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.IndexDbServiceImpl;
import org.docear.syncdaemon.projects.Project;

public class FileIndexServiceImpl implements FileIndexService {

	private final ClientService clientService;
	private final IndexDbService indexDbService;
	private final Daemon daemon;
	
	public FileIndexServiceImpl(Daemon daemon){
		this.daemon = daemon;
		clientService = daemon.service(ClientServiceImpl.class);
		indexDbService = daemon.service(IndexDbServiceImpl.class);
	}
	
	@Override
	public void scanProject(Project project) {
		long localRev = indexDbService.getProjectRevision(project.getId());
		
		if (localRev != project.getRevision()){
			
			//
			FileMetaData rootMetaData = new FileMetaData(project.getId(), ".", false);
			FolderMetaData root = clientService.getFolderMetaData(rootMetaData);
			
			
			
			//TODO for folders
	        final FileMetaData fromScan = null;//TODO this class
	        final FileMetaData fromIndexDb = null;//TODO
	        if (fromScan.isChanged(fromIndexDb)) {
	        	
	        } else {
	            //akka message if it would be jNotify and life change
	        }
		}
	}

}
