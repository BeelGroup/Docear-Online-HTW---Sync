package org.docear.syncdemon.actors;

import akka.actor.UntypedActor;
import org.docear.syncdemon.messages.FileCreatedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogActor extends UntypedActor {
    private static final Logger logger = LoggerFactory.getLogger(LogActor.class);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof FileCreatedMessage) {
            logger.info(message.toString());
        } else {
            unhandled(message);
        }
    }
}
