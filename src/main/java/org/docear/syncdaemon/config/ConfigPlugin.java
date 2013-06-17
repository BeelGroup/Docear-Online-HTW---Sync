package org.docear.syncdaemon.config;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ConfigPlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(ConfigPlugin.class);
    
	public ConfigPlugin(Daemon daemon) {
		super(daemon);
	}

	@Override
	public void onStart() {
        final File syncDaemonHome = daemon().service(ConfigService.class).getSyncDaemonHome();
        logger.info("sync daemon home=" + syncDaemonHome);
    }

	@Override
	public void onStop() {
		
	}
	
}
