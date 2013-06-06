package org.docear.syncdaemon.actors;

import org.docear.syncdaemon.messages.FileChangeEvent;
import org.docear.syncdaemon.messages.FileConflictEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class LogActor extends UntypedActor {
    private static final Logger logger = LoggerFactory.getLogger(LogActor.class);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof FileChangeEvent 
        	|| message instanceof FileConflictEvent) {
            logger.info(message.toString());
        } else {
            unhandled(message);
        }
    }
}
