package org.docear.syncdaemon.fileactors;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import org.apache.commons.io.FileUtils;
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


            //validate not null and hash
            if (fileMetaDataFS == null) {
                throw new NullPointerException("fileMetaDataFS cannot be null");
            }
            //something is present at location
            else {
                final FileMetaData fileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataFS);
                //check if folder
                if (fileMetaDataFS.isFolder()) {
                    //check that indexDB does not know a folder
                    if(!fileMetaDataDB.isFolder()) {
                        //upload the folder
                        clientService.upload(user,project,fileMetaDataDB);
                    }
                }
                if (!fileMetaDataFS.isDeleted() && !fileMetaDataFS.isFolder() && !hashAlgorithm.isValidHash(fileMetaDataFS.getHash())) {
                    throw new IllegalArgumentException("No valid hash for FS file");
                }


                //final FileMetaData fileMetaDataServer = clientService.getCurrentFileMetaData(user, fileMetaDataFS);

                //1a. look if locally new file
                if (fileMetaDataDB == null || fileMetaDataDB.isDeleted()) {
                    final UploadResponse uploadResponse = clientService.upload(user, project, fileMetaDataFS);
                    //Conflict?
                    if (uploadResponse.hasConflicts()) {
                        //download real file, conflicted will be triggered by update listener
                        downloadAndPutFile(project, uploadResponse.getCurrentServerMetaData());
                    } else {
                        indexDbService.save(uploadResponse.getCurrentServerMetaData());
                    }
                }
                //1b. is locally deleted
                else if ((fileMetaDataFS.isDeleted() && !fileMetaDataDB.isDeleted())) {
                    clientService.delete(user, project, fileMetaDataDB);
                }
                //1c. is locally updated file
                else if (!fileMetaDataFS.getHash().equals(fileMetaDataDB.getHash())) {// locally changed)
                    //1b.1 push new file to db
                    final UploadResponse uploadResponse = clientService.upload(user, project, fileMetaDataFS);
                    //check for conflicts
                    if (uploadResponse.hasConflicts()) {
                        //download correct file, conflicted will be triggered by update listener
                        downloadAndPutFile(project, uploadResponse.getCurrentServerMetaData());
                    }
                }
            }
        } else if (message instanceof Messages.FileChangedOnServer) {

            final Messages.FileChangedOnServer fileChangedOnServer = (Messages.FileChangedOnServer) message;
            final Project project = fileChangedOnServer.getProject();
            final FileMetaData fileMetaDataServer = fileChangedOnServer.getFileMetaDataOnServer();

            final FileMetaData fileFileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataServer);
            final FileMetaData fileMetaDataFS = getFSMetadata(project, fileMetaDataServer);

            // check if file is NOT locally present
            if (fileMetaDataFS == null && fileFileMetaDataDB == null) {
                downloadAndPutFile(project, fileMetaDataServer);
            }
            // check if file is as said in DB
            else if ((fileMetaDataFS == null && fileFileMetaDataDB.isDeleted()) ||
                    fileMetaDataFS.getHash().equals(fileFileMetaDataDB.getHash())) {
                //YES
                //check if indexDB is different than server
                if (fileFileMetaDataDB.getRevision() != fileMetaDataServer.getRevision()) {
                    //YES
                    //check if file shall be deleted or upserted
                    if (fileMetaDataServer.isDeleted()) {
                        //deleted
                        deleteFile(project, fileMetaDataFS);
                    } else {
                        //upserted
                        //check if folder or file
                        if (fileMetaDataServer.isFolder()) {
                            getFile(project, fileMetaDataServer).mkdir();
                        } else {
                            downloadAndPutFile(project, fileMetaDataServer);
                        }
                        //put metadata
                        indexDbService.save(fileMetaDataServer);
                    }
                }
            }
        } else if (message instanceof User) {
            this.user = (User) message;
        } else if (message instanceof Messages.ProjectDeleted) {
            final Messages.ProjectDeleted projectDeleted = (Messages.ProjectDeleted) message;

            // remove files from FS
            FileUtils.deleteDirectory(new File(projectDeleted.getProject().getRootPath()));

            indexDbService.deleteProject(projectDeleted.getProject().getId());
        } else if (message instanceof Messages.ProjectAdded) {
            final Messages.ProjectAdded projectAdded = (Messages.ProjectAdded) message;

            // create root dir in FS
            FileUtils.forceMkdir(new File(projectAdded.getProject().getRootPath()));

            indexDbService.addProject(projectAdded.getProject().getId(), projectAdded.getProject().getRootPath());
        }
    }

    private FileMetaData getFSMetadata(Project project, FileMetaData fileMetaData) throws IOException {
        final String path = project.getRootPath() + File.separator + fileMetaData.getPath();
        final File file = new File(path);
        if (file.exists()) {
            final String hash = hashAlgorithm.generate(file);

            return new FileMetaData(fileMetaData.getPath(), hash, project.getId(), fileMetaData.isFolder(), fileMetaData.isDeleted(), fileMetaData.getRevision());
        } else {
            return null;
        }
    }

    private void deleteFile(Project project, FileMetaData fileMetaData) throws IOException {
        final File file = getFile(project, fileMetaData);
        if (file.exists() && !file.delete()) {
            throw new IOException("could not delete file");
        }
    }

    private void downloadAndPutFile(Project project, FileMetaData fileMetaData) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            deleteFile(project, fileMetaData);

            final File file = getFile(project, fileMetaData);

            in = clientService.download(user, fileMetaData);
            out = new FileOutputStream(file);

            IOUtils.copy(in, out);
        } catch (IOException e) {
            //problem deleting file. May be locked
            //scheduling a retry in 30 seconds
            final ActorSystem system = getContext().system();
            system.scheduler().scheduleOnce(Duration.apply(30, TimeUnit.SECONDS), getSelf(), new Messages.FileChangedOnServer(project, fileMetaData), system.dispatcher());
            throw new IOException("Could not delete file. It may be locked. Rescheduled event in 30 seconds.");
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    private File getFile(Project project, FileMetaData fileMetaData) {
        final String path = project.getRootPath() + File.separator + fileMetaData.getPath();
        return new File(path);
    }
}
