package org.docear.syncdaemon.config;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ConfigPlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(ConfigPlugin.class);
    private ConfigService configService;
    
	public ConfigPlugin(Daemon daemon) {
		super(daemon);
	}

	@Override
	public void onStart() {
        logger.info("Config Plugin starting...");
        configService = daemon().service(ConfigService.class);
        final File syncDaemonHome = configService.getSyncDaemonHome();
        logger.info("sync daemon home=" + syncDaemonHome);
        logger.info("Config Plugin started successfully");
    }

	@Override
	public void onStop() {
		
	}
	
}
