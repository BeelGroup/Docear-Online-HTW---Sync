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
        logger.info(name() + " starts");
        logger.info("baseurl=" + baseUrl());
    }

    @Override
    public void onStop() {
        logger.info(name() + " stops");
    }

    private String baseUrl() {
        return daemon().getConfig().getString("daemon.client.baseurl");
    }

    private String name() {
        return daemon().getConfig().getString("daemon.name");
    }
}
