package org.docear.syncdaemon;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Daemon {

    private Config config;

    public Daemon() {
        config = ConfigFactory.load();
    }

    public Daemon(Config config) {
        this.config = config;
    }

    public static Daemon createWithAdditionalConfig(final Config config) {
        final Daemon daemon = new Daemon();
        daemon.config = config.withFallback(daemon.config);
        return daemon;
    }

    public <T extends Plugin> T plugin(Class<T> clazz) {
		throw new RuntimeException("Not implemented");
	}

	public <T> T service(Class<T> clazz) {
		throw new RuntimeException("Not implemented");
	}
	
	private void onStart() {
		/**
		 * - Actor System for internal and external communication
		 * - RUNNING_PID with PID 
		 * - RUNNING_PORT with port of actor system
		 * - initialize index-db
		 * - initialize file system
		 *   - start jNotify
		 * - refresh index
		 */
	}
	
	private void onStop() {
		
	}

    public Config getConfig() {
        return config;
    }
}
