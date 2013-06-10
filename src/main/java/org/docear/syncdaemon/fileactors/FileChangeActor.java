package org.docear.syncdaemon.fileactors;

import akka.actor.UntypedActor;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.indexdb.IndexDbService;

public class FileChangeActor extends UntypedActor {

    private ClientService clientService;
    private IndexDbService indexDbService;

    public FileChangeActor(ClientService clientService, IndexDbService indexDbService) {
        this.clientService = clientService;
        this.indexDbService = indexDbService;
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof Messages.FileChangedLocally) {
            /**
             * TODO https://docs.google.com/document/d/17ZmlL8di7RWdSowJr-jXrSd7WBkubZ9d_kCxYZN-_vc/edit#
             1. Datei anschauen
             1a. Schauen ob anders als in Index-DB
             1.1.a JA
             1.1.b Ist Index-DB anders als online
             1.1.1a JA
             1.1.1b Conflicted Version
             1.1.1c Version von Server als aktuelle ziehen
             1.1.1d Index-DB updaten

             1.1.2a NEIN
             1.1.2b aktuelle Datei auf server pushen
             1.1.2c Index-Db mit metadaten der hochgeladenen Datei von Server updaten


             */

        } else if(message instanceof Messages.FileChangedOnServer) {
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
        } else if (message instanceof Messages.ProjectChangedOnServer){
        	
        }
        //TODO project added, removed
    }
}
