package org.docear.syncdaemon.logging;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);

    public LoggingPlugin(Daemon daemon) {
        super(daemon);
    }

    @Override
    public void onStart() {
        logger.info("daemon starts");
    }

    @Override
    public void onStop() {
        logger.info("daemon stops");
    }
}
