package org.docear.syncdaemon.config;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;

public class ConfigPlugin extends Plugin{

	private ConfigService configService;
	
	public ConfigPlugin(Daemon daemon) {
		super(daemon);
	}

	@Override
	public void onStart() {
		configService = daemon().service(ConfigService.class);
	}

	@Override
	public void onStop() {
		configService = null;
	}
	
}
