package org.docear.syncdaemon.fileactors;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.UploadResponse;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.jnotify.Listener;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FileChangeActor extends UntypedActor {

    private static final Map<String, Long> ResourceLastActionMap = new HashMap<String, Long>();
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(FileChangeActor.class);
    private final HashAlgorithm hashAlgorithm = new SHA2();
    private final TempFileService tempFileService;
    private final ActorSystem tempFileActorSystem;
    private ClientService clientService;
    private IndexDbService indexDbService;
    private User user;
    private Set<Integer> jNotifyWatchIds = new HashSet<Integer>();

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
            final String path = fileChangedLocally.getPath();

            // if file is temp file that doesn't exist the defined amount of time, send the message again
            if (tempFileService.isTempFile(path)) {
                logger.debug("temp file detected, rescheduling");
                tempFileActorSystem.scheduler().scheduleOnce(Duration.create(tempFileService.getTimeOutMillis(), TimeUnit.MILLISECONDS), this.getSelf(), message, tempFileActorSystem.dispatcher());
            }

            if (!ignoreResource(project, path)) {
                setResourceLastAction(fileChangedLocally.getProject(), path);
                fileChangedLocally(fileChangedLocally);
            }
        } else if (message instanceof Messages.FileChangedOnServer) {
            final Messages.FileChangedOnServer fileChangedOnServer = (Messages.FileChangedOnServer) message;
            final Project project = fileChangedOnServer.getProject();
            final FileMetaData fileMetaData = fileChangedOnServer.getFileMetaDataOnServer();

            setResourceLastAction(project, fileMetaData.getPath());
            fileChangedOnServer(fileChangedOnServer);

        } else if (message instanceof User) {
            this.user = (User) message;
        } else if (message instanceof Messages.ProjectDeleted) {
            final Messages.ProjectDeleted projectDeleted = (Messages.ProjectDeleted) message;

            // (no deletion)
            //FileUtils.deleteDirectory(new File(projectDeleted.getProject().getRootPath()));

            indexDbService.deleteProject(projectDeleted.getProject().getId());
        } else if (message instanceof Messages.ProjectAdded) {
            final Messages.ProjectAdded projectAdded = (Messages.ProjectAdded) message;
            final Project project = projectAdded.getProject();

            // create root dir in FS
            FileUtils.forceMkdir(new File(project.getRootPath()));

            //create jNotifyWatch
            final Listener listener = new Listener(project, getSelf());
            try {
                jNotifyWatchIds.add(JNotify.addWatch(project.getRootPath(), JNotify.FILE_ANY, true, listener));
            } catch (JNotifyException e) {
                logger.error("could not create watch on project folder!", e);
            }
        }
    }

    private void setResourceLastAction(Project project, String path) {
        final String resource = project.getRootPath() + "/" + path;
        ResourceLastActionMap.put(resource, System.currentTimeMillis());
    }

    private boolean ignoreResource(Project project, String path) {
        final String resource = project.getRootPath() + "/" + path;
        final boolean ignore = ResourceLastActionMap.containsKey(resource) && (System.currentTimeMillis() - ResourceLastActionMap.get(resource)) < 1000;
        logger.debug("ignoreResource => " + ignore);
        return ignore;
    }

    private void fileChangedLocally(Messages.FileChangedLocally fileChangedLocally) throws IOException {
        final Project project = fileChangedLocally.getProject();
        final String filePath = fileChangedLocally.getPath();


        //validate not null
        if (filePath == null) {
            throw new NullPointerException("file path cannot be null");
        }
        //something is present at location
        else {
            final FileMetaData fileMetaDataFS = FileMetaData.fromFS(hashAlgorithm, project.getId(), project.getRootPath(), filePath);

            logger.debug("fcl => FS Meta: " + fileMetaDataFS);
            final FileMetaData fileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataFS);
            logger.debug("fcl => DB Meta: " + fileMetaDataDB);

            //check if equal with indexDB
            if (((fileMetaDataFS.isFolder() && fileMetaDataDB != null && fileMetaDataDB.isFolder() && !fileMetaDataDB.isDeleted()) ||
                    (fileMetaDataFS.isDeleted() && (fileMetaDataDB == null || fileMetaDataDB.isDeleted())) ||
                    (!fileMetaDataFS.getHash().isEmpty() && fileMetaDataDB != null && fileMetaDataFS.getHash().equals(fileMetaDataDB.getHash())))) {
                logger.debug("fcl => equal, nothing to do");
                return;
            }

            //check if deleted (independent from file/folder
            else if ((fileMetaDataFS.isDeleted() && fileMetaDataDB != null && !fileMetaDataDB.isDeleted())) {
                logger.debug("fcl => file deleted, removing on server");
                final FileMetaData fileMetaDataServer = clientService.delete(user, project, fileMetaDataDB);
                indexDbService.save(fileMetaDataServer);
            }
            //check if folder
            else if (fileMetaDataFS.isFolder()) {
                logger.debug("fcl => is folder");
                //check that indexDB does not know a folder
                if (fileMetaDataDB == null || !fileMetaDataDB.isFolder() || fileMetaDataDB.isDeleted()) {
                    logger.debug("fcl => is new folder");
                    //upload the folder
                    final FileMetaData fileMetaDataServer = clientService.createFolder(user, project, fileMetaDataFS);
                    indexDbService.save(fileMetaDataServer);
                }
            }
            //is existing file
            else {
                if (!hashAlgorithm.isValidHash(fileMetaDataFS.getHash())) {
                    throw new IllegalArgumentException("No valid hash for FS file: " + fileMetaDataFS.getHash());
                }


                UploadResponse uploadResponse = null;
                //look if locally new file
                if (fileMetaDataDB == null || fileMetaDataDB.isDeleted()) {
                    logger.debug("fcl => is new file");
                    //revision doesn't matter, because file is not present online (assumption)
                    uploadResponse = clientService.upload(user, project, fileMetaDataFS);
                }
                //is locally updated file
                else if (!fileMetaDataFS.getHash().equals(fileMetaDataDB.getHash())) {
                    logger.debug("fcl => is updated file");
                    //create meta data with correct revision
                    final FileMetaData correctMetaData = FileMetaData.file(fileMetaDataFS.getPath(), fileMetaDataFS.getHash(), project.getId(), false, fileMetaDataDB.getRevision());
                    //send request
                    uploadResponse = clientService.upload(user, project, correctMetaData);
                }

                if (uploadResponse != null) {
                    //Conflict?
                    if (uploadResponse.hasConflicts()) {
                        logger.debug("fcl => conflict created at upload");
                        //download real file, conflicted will be triggered by update listener
                        downloadAndPutFile(project, uploadResponse.getCurrentServerMetaData());
                    }
                    //save in index db
                    final FileMetaData mixedMeta = new FileMetaData(fileMetaDataFS.getPath(), fileMetaDataFS.getHash(), fileMetaDataFS.getProjectId(), false, false, uploadResponse.getCurrentServerMetaData().getRevision());
                    indexDbService.save(mixedMeta);
                }
            }
        }
    }

    private void fileChangedOnServer(Messages.FileChangedOnServer fileChangedOnServer) throws IOException {
        final Project project = fileChangedOnServer.getProject();
        final FileMetaData fileMetaDataServer = fileChangedOnServer.getFileMetaDataOnServer();
        final File file = getFile(project, fileMetaDataServer);

        final FileMetaData fileMetaDataDB = indexDbService.getFileMetaData(fileMetaDataServer);

        logger.debug("fcos => DB Meta: " + fileMetaDataDB);
        logger.debug("fcos => SV Meta: " + fileMetaDataServer);

        // check if server revision is already known
        if (fileMetaDataDB != null && fileMetaDataDB.getRevision() == fileMetaDataServer.getRevision()) {
            logger.debug("fcos => file already up to date");
            return;
        }
        // check if file/folder has been deleted
        else if (fileMetaDataServer.isDeleted()) {
            logger.debug("fcos => deleted on server, deleting locally");

            //delete if not already deleted
            if(fileMetaDataDB != null && !fileMetaDataDB.isDeleted())
                FileUtils.forceDelete(file);

            indexDbService.save(fileMetaDataServer);
        }
        // check if new file is a folder
        else if (fileMetaDataServer.isFolder()) {
            logger.debug("fcos => folder, creating folder tree");
            if (file.exists())
                file.delete();
            file.mkdirs();
            indexDbService.save(fileMetaDataServer);
        }
        // is an on the server existing file
        else {
            logger.debug("fcos => updated file");
            //check if hash is identical
            if (fileMetaDataDB == null || !fileMetaDataDB.getHash().equals(fileMetaDataServer.getHash())) {
                logger.debug("fcos => hash different, downloading new file");
                downloadAndPutFile(project, fileMetaDataServer);
            }

            logger.debug("fcos => saving new meta data");
            indexDbService.save(fileMetaDataServer);
        }
    }

    private void downloadAndPutFile(Project project, FileMetaData fileMetaData) throws IOException {
        InputStream in = null;
//        OutputStream out = null;
        try {
            //deleteFile(project, fileMetaData);
            final File file = getFile(project, fileMetaData);

            logger.debug("downloadAndPutFile => local file: " + file.getAbsoluteFile());
            in = clientService.download(user, fileMetaData);

            if (in == null) {
                logger.error("Could not find File online");
            } else {
                logger.debug("downloadAndPutFile => downloading and writing");
                //file.getParentFile().mkdirs();
                FileUtils.copyInputStreamToFile(in, file);
//                out = new FileOutputStream(file);
//
//                IOUtils.copy(in, out);
            }
        } catch (IOException e) {
            //problem deleting file. May be locked
            //scheduling a retry in 30 seconds
            final ActorSystem system = getContext().system();
            system.scheduler().scheduleOnce(Duration.apply(30, TimeUnit.SECONDS), getSelf(), new Messages.FileChangedOnServer(project, fileMetaData), system.dispatcher());
            logger.warn("Could not download file. It may be locked. Rescheduled event in 30 seconds.", e);
        } finally {
            IOUtils.closeQuietly(in);
//            IOUtils.closeQuietly(out);
        }
    }

    private File getFile(Project project, FileMetaData fileMetaData) {
        final String path = project.getRootPath() + File.separator + fileMetaData.getPath();
        return new File(path);
    }
}
