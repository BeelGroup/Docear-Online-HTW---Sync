package org.docear.syncdaemon.fileactors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ListenForUpdatesResponse;
import org.docear.syncdaemon.users.User;

import java.util.Map;

public class ListenForUpdatesActor extends UntypedActor {

    private User user;
    private ClientService clientService;
    private ActorRef fileChangeActor;

    private Map<String, Long> projectIdRevisonMap;

    public ListenForUpdatesActor(User user, ClientService clientService, ActorRef fileChangeActor) {
        this.user = user;
        this.clientService = clientService;
        this.fileChangeActor = fileChangeActor;
    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Messages.StartListening) {
            //TODO get current project
        } else if(message instanceof ListenForUpdatesResponse) {
            //TODO send changes to file change actor
            //TODO call self again
        } else if(message.equals("listen")) {
            //TODO listen again
        }
    }
}
