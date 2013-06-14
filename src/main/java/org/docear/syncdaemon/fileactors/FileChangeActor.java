package org.docear.syncdaemon.fileactors;

import akka.actor.UntypedActor;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.UploadResponse;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;

import java.io.File;
import java.io.IOException;

public class FileChangeActor extends UntypedActor {

    private ClientService clientService;
    private IndexDbService indexDbService;
    private User user;
    private final HashAlgorithm hashAlgorithm = new SHA2();

    public FileChangeActor(ClientService clientService, IndexDbService indexDbService, User user) {
        this.clientService = clientService;
        this.indexDbService = indexDbService;
        this.user = user;
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Messages.FileChangedLocally) {
            final Messages.FileChangedLocally fileChangedLocally = (Messages.FileChangedLocally) message;
            final Project project = fileChangedLocally.getProject();
            final FileMetaData fileMetaDataFS = fileChangedLocally.getFileMetaDataLocally();

            //validate hash
            if(!hashAlgorithm.isValidHash(fileMetaDataFS.getHash())) {
                throw new IllegalArgumentException("No valid hash for new file");
            }

            final FileMetaData fileFileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataFS);
            final FileMetaData fileMetaDataServer = clientService.getCurrentFileMetaData(user, fileMetaDataFS);

            //1. show if FS different from DB
            if (!fileMetaDataFS.getHash().equals(fileFileMetaDataDB.getHash())) {
                //YES
                //1.1 Is index-DB up to date
                if (fileFileMetaDataDB.getRevision() == fileMetaDataServer.getRevision()) {
                    // YES, up to date
                    // 1.1.1 push file to server
                    final UploadResponse uploadResponse = clientService.upload(user, project, fileFileMetaDataDB);
                    if (uploadResponse.hasConflicts()) {
                        //CONFLICT (may happen because two people push at the same time)
                        //TODO handle conflict situation
                    } else {
                        //everything is fine, just update the indexDB
                        indexDbService.save(uploadResponse.getCurrentServerMetaData());
                    }
                }
            }
        } else if (message instanceof Messages.FileChangedOnServer) {

            final Messages.FileChangedOnServer fileChangedOnServer = (Messages.FileChangedOnServer) message;
            final Project project = fileChangedOnServer.getProject();
            final FileMetaData fileMetaDataServer = fileChangedOnServer.getFileMetaDataOnServer();

            final FileMetaData fileFileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataServer);


            final FileMetaData fileMetaDataFS = getFSMetadata(project,fileFileMetaDataDB);
            /**
             * see https://docs.google.com/document/d/17ZmlL8di7RWdSowJr-jXrSd7WBkubZ9d_kCxYZN-_vc/edit#
             * 1. Datei anschauen
             1a. Schauen ob anders als in Index-DB
             1.2a NEIN
             1.2b Schauen ob Index-DB anders als Server
             1.2.1a JA
             1.2.1b Akteuelle Datei von Server ziehen
             1.2.1c Index-DB mit Metadaten von server updaten

             1.2.2a NEIN
             1.2.2b alles up to date, nichts tun
             */
        } else if (message instanceof Messages.ProjectUpdated){
        	
        } else if (message instanceof Messages.ProjectAdded){
        	
        } else if (message instanceof Messages.ProjectDeleted){
        	
        } else if (message instanceof User) {
            this.user = (User) message;
        }
    }

    private FileMetaData getFSMetadata(Project project, FileMetaData fileMetaData) throws IOException {
        final String path = project.getRootPath()+ File.separator+fileMetaData.getPath();
        final File file = new File(path);
        final String hash = hashAlgorithm.generate(file);

        return new FileMetaData(fileMetaData.getPath(),hash,project.getId(),fileMetaData.isFolder(),fileMetaData.isDeleted(),fileMetaData.getRevision());
    }
}
