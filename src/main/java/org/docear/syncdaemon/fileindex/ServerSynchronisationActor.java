package org.docear.syncdaemon.fileindex;

import akka.actor.UntypedActor;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.UploadResponse;
import org.docear.syncdaemon.fileindex.messages.LocalFileChanged;
import org.docear.syncdaemon.indexdb.IndexDbService;

public class ServerSynchronisationActor extends UntypedActor {

    private final ClientService clientService;
    private final IndexDbService indexDbService;

    public ServerSynchronisationActor(ClientService clientService, IndexDbService indexDbService) {
        this.clientService = clientService;
        this.indexDbService = indexDbService;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof LocalFileChanged) {
            //TODO with dispatcher
            final FileMetaData fileMetaData = ((LocalFileChanged) message).getFileMetaData();
            final UploadResponse uploadResponse = clientService.upload(fileMetaData);
            if (uploadResponse.hasConflicts()) {
                //TODO rename conflicted file
                //TODO suppress jNotify events for download
                clientService.download(uploadResponse.getCurrentServerMetaData());
                indexDbService.save(uploadResponse.getConflictedServerMetaData());
            }
            indexDbService.save(uploadResponse.getCurrentServerMetaData());
        }
    }
}
