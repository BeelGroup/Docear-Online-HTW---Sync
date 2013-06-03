package org.docear.syncdaemon.fileindex;

import akka.actor.UntypedActor;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.fileindex.messages.LocalFileChanged;
import org.docear.syncdaemon.indexdb.IndexDbService;

public class ServerSynchronisationActor extends UntypedActor {

    private final ClientService clientService;
    private final IndexDbService indexDbService;

    public ServerSynchronisationActor(ClientService clientService, IndexDbService indexDbService) {
        this.clientService = clientService;
        this.indexDbService = indexDbService;
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof LocalFileChanged) {
            //TODO with dispatcher
            final FileMetaData fileMetaData = ((LocalFileChanged) message).getFileMetaData();




            final FileMetaData serverFileMetaData = clientService.getFileMetaData(fileMetaData);
            if (fileMetaData.isChanged(serverFileMetaData)) {

            }
            IndexDbService.save(serverFileMetaData);
        }
    }
}
