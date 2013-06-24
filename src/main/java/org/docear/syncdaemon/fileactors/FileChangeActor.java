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
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FileChangeActor extends UntypedActor {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(FileChangeActor.class);
    private final HashAlgorithm hashAlgorithm = new SHA2();
    private ClientService clientService;
    private IndexDbService indexDbService;
    private User user;
    private static final Map<String,Long> ResourceLastActionMap = new HashMap<String, Long>();
    private final TempFileService tempFileService;
    private final ActorSystem tempFileActorSystem;

    public FileChangeActor(ClientService clientService, IndexDbService indexDbService, User user, TempFileService tempFileService) {
        this.clientService = clientService;
        this.indexDbService = indexDbService;
        this.user = user;
        this.tempFileService = tempFileService;
        this.tempFileActorSystem = ActorSystem.apply();
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
            final FileMetaData fileMetaData = fileChangedLocally.getFileMetaDataLocally();
            
            // if file is temp file that doesn't exist the defined amount of time, send the message again
            if (tempFileService.isTempFile(fileMetaData)){
            	tempFileActorSystem.scheduler().scheduleOnce(Duration.create(tempFileService.getTimeOutMillis(), TimeUnit.MILLISECONDS), this.getSelf(), message, tempFileActorSystem.dispatcher());
            }
            
            if(!ignoreResource(project,fileMetaData)) {
                setResourceLastAction(fileChangedLocally.getProject(),fileChangedLocally.getFileMetaDataLocally());
                fileChangedLocally(fileChangedLocally);
            }
        } else if (message instanceof Messages.FileChangedOnServer) {
            final Messages.FileChangedOnServer fileChangedOnServer = (Messages.FileChangedOnServer) message;
            final Project project = fileChangedOnServer.getProject();
            final FileMetaData fileMetaData = fileChangedOnServer.getFileMetaDataOnServer();
            if(!ignoreResource(project,fileMetaData)) {
                setResourceLastAction(fileChangedOnServer.getProject(),fileChangedOnServer.getFileMetaDataOnServer());
                fileChangedOnServer(fileChangedOnServer);
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


            //TODO add project
        }
    }

    private void setResourceLastAction(Project project, FileMetaData fileMetaData) {
        final String resource = project.getRootPath()+"/"+fileMetaData.getPath();
        ResourceLastActionMap.put(resource,System.currentTimeMillis());
    }

    private boolean ignoreResource(Project project, FileMetaData fileMetaData) {
        final String resource = project.getRootPath()+"/"+fileMetaData.getPath();

        return ResourceLastActionMap.containsKey(resource) && (System.currentTimeMillis() - ResourceLastActionMap.get(resource)) < 1000;
    }

    private void fileChangedLocally(Messages.FileChangedLocally fileChangedLocally) throws IOException {
        final Project project = fileChangedLocally.getProject();
        final FileMetaData fileMetaDataFS = fileChangedLocally.getFileMetaDataLocally();


        //validate not null and hash
        if (fileMetaDataFS == null) {
            throw new NullPointerException("fileMetaDataFS cannot be null");
        }
        //something is present at location
        else {
            final FileMetaData fileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataFS);

            //check if deleted (independent from file/folder
            if ((fileMetaDataFS.isDeleted() && !fileMetaDataDB.isDeleted())) {
                final FileMetaData fileMetaDataServer = clientService.delete(user, project, fileMetaDataDB);
                indexDbService.save(fileMetaDataServer);
            }
            //check if folder
            else if (fileMetaDataFS.isFolder()) {
                //check that indexDB does not know a folder
                if (fileMetaDataDB == null || !fileMetaDataDB.isFolder() || fileMetaDataDB.isDeleted()) {
                    //upload the folder
                    final FileMetaData fileMetaDataServer = clientService.createFolder(user, project, fileMetaDataFS);
                    indexDbService.save(fileMetaDataServer);
                }
            }
            //is existing file
            else {
                if (!hashAlgorithm.isValidHash(fileMetaDataFS.getHash())) {
                    throw new IllegalArgumentException("No valid hash for FS file: "+fileMetaDataFS.getHash());
                }

                UploadResponse uploadResponse = null;
                //look if locally new file
                if (fileMetaDataDB == null || fileMetaDataDB.isDeleted()) {
                    //revision doesn't matter, because file is not present online (assumption)
                    uploadResponse = clientService.upload(user, project, fileMetaDataFS);
                }
                //is locally updated file
                else if (!fileMetaDataFS.getHash().equals(fileMetaDataDB.getHash())) {
                    //create meta data with correct revision
                    final FileMetaData correctMetaData = FileMetaData.file(fileMetaDataFS.getPath(), fileMetaDataFS.getHash(), project.getId(), false, fileMetaDataDB.getRevision());
                    //send request
                    uploadResponse = clientService.upload(user, project, correctMetaData);
                }

                if (uploadResponse != null) {
                    //Conflict?
                    if (uploadResponse.hasConflicts()) {
                        //download real file, conflicted will be triggered by update listener
                        downloadAndPutFile(project, uploadResponse.getCurrentServerMetaData());
                    }
                    //save in index db
                    indexDbService.save(uploadResponse.getCurrentServerMetaData());
                }
            }
        }
    }

    private void fileChangedOnServer(Messages.FileChangedOnServer fileChangedOnServer) throws IOException {
        final Project project = fileChangedOnServer.getProject();
        final FileMetaData fileMetaDataServer = fileChangedOnServer.getFileMetaDataOnServer();
        final File file = getFile(project, fileMetaDataServer);

        final FileMetaData fileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataServer);
        //final FileMetaData fileMetaDataFS = getFSMetadata(project, fileMetaDataServer);

        // check if server revision is already known
        if (fileMetaDataDB != null && fileMetaDataDB.getRevision() == fileMetaDataServer.getRevision()) {
            // nothing to do;
            return;
        }
        // check if file/folder has been deleted
        else if (fileMetaDataServer.isDeleted()) {
            file.delete();
            indexDbService.save(fileMetaDataServer);
        }
        // check if new file is a folder
        else if (fileMetaDataServer.isFolder()) {
            if (file.exists())
                file.delete();
            file.mkdirs();
            indexDbService.save(fileMetaDataServer);
        }
        // is an on the server existing file
        else {
            downloadAndPutFile(project, fileMetaDataServer);
            indexDbService.save(fileMetaDataServer);
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

            if (in == null) {
                logger.error("Could not find File online");
            } else {
                file.getParentFile().mkdirs();
                out = new FileOutputStream(file);

                IOUtils.copy(in, out);
            }
        } catch (IOException e) {
            //problem deleting file. May be locked
            //scheduling a retry in 30 seconds
            final ActorSystem system = getContext().system();
            system.scheduler().scheduleOnce(Duration.apply(30, TimeUnit.SECONDS), getSelf(), new Messages.FileChangedOnServer(project, fileMetaData), system.dispatcher());
            logger.warn("Could not download file. It may be locked. Rescheduled event in 30 seconds.", e);
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
