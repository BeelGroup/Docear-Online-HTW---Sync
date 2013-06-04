package org.docear.syncdaemon.fileindex;

import java.util.List;

import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ClientServiceImpl;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.IndexDbServiceImpl;
import org.docear.syncdaemon.projects.Project;

public class FileIndexServiceImpl implements FileIndexService {

	private final ClientService clientService;
	private final IndexDbService indexDbService;
	
	public FileIndexServiceImpl(){
		clientService = new ClientServiceImpl();
		indexDbService = new IndexDbServiceImpl();
	}
	
	@Override
	public void scanProject(Project project) {
		long localRev = indexDbService.getProjectRevision(project.getId());
		
		if (localRev != project.getRevision()){
			List<FileMetaData> files = FileReceiver.receiveFiles(project);
			
			for (FileMetaData fmdFromScan : files){			
		        final FileMetaData fmdFromIndexDb = indexDbService.getFileMetaData(fmdFromScan);
		        if (fmdFromScan.isChanged(fmdFromIndexDb)) {
		        	
		        } else {
		            //akka message if it would be jNotify and life change
		        }
			}
		}
	}

}
