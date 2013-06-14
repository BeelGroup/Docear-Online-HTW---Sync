package org.docear.syncdaemon.fileactors;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import org.apache.commons.io.IOUtils;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.UploadResponse;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class FileChangeActor extends UntypedActor {

    private final HashAlgorithm hashAlgorithm = new SHA2();
    private ClientService clientService;
    private IndexDbService indexDbService;
    private User user;

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
            if (!hashAlgorithm.isValidHash(fileMetaDataFS.getHash())) {
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
            final FileMetaData fileMetaDataFS = getFSMetadata(project, fileFileMetaDataDB);

            // check if file is as said in DB
            if (fileMetaDataFS.getHash().equals(fileFileMetaDataDB.getHash())) {
                //YES
                //check if indexDB is different than server
                if (fileFileMetaDataDB.getRevision() != fileMetaDataServer.getRevision()) {
                    //YES
                    //download and put file
                    downloadAndPutFile(project,fileMetaDataServer);
                }
            }
            /**
             1.2b Schauen ob Index-DB anders als Server
             1.2.1a JA
             1.2.1b Akteuelle Datei von Server ziehen
             1.2.1c Index-DB mit Metadaten von server updaten

             1.2.2a NEIN
             1.2.2b alles up to date, nichts tun
             */
        } else if (message instanceof User) {
            this.user = (User) message;
        }
        //TODO project added, removed
    }

    private FileMetaData getFSMetadata(Project project, FileMetaData fileMetaData) throws IOException {
        final String path = project.getRootPath() + File.separator + fileMetaData.getPath();
        final File file = new File(path);
        final String hash = hashAlgorithm.generate(file);

        return new FileMetaData(fileMetaData.getPath(), hash, project.getId(), fileMetaData.isFolder(), fileMetaData.isDeleted(), fileMetaData.getRevision());
    }

    private void downloadAndPutFile(Project project, FileMetaData fileMetaData) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            final String path = project.getRootPath() + File.separator + fileMetaData.getPath();
            final File file = new File(path);
            if (file.exists() && !file.delete()) {
                //problem deleting file. May be locked
                //scheduling a retry in 30 seconds
                final ActorSystem system = getContext().system();
                system.scheduler().scheduleOnce(Duration.apply(30, TimeUnit.SECONDS), getSelf(), new Messages.FileChangedOnServer(project, fileMetaData), system.dispatcher());
                throw new IOException("Could not delete file. It may be locked. Rescheduled event in 30 seconds.");
            }

            in = clientService.download(user, fileMetaData);
            out = new FileOutputStream(file);

            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
