package org.docear.syncdemon.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class SyncDispatchActor extends UntypedActor {
    final ActorRef logActor = getContext().actorFor("../logger");

    @Override
    public void onReceive(Object message) throws Exception {
        logActor.tell(message);
    }
}
