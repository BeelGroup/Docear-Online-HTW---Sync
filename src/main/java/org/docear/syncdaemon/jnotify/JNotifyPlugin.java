package org.docear.syncdaemon.jnotify;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class JNotifyPlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(JNotifyPlugin.class);

    public JNotifyPlugin(Daemon daemon) {
        super(daemon);
    }

    @Override
    public void onStart() {
        logger.info("JNotify Plugin starting...");
        try {
            new NativeLibraryResolver(new File(".")).run();
            logger.info("loaded jnotify");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("JNotify Plugin started!");
    }

    @Override
    public void onStop() {
    }
}
