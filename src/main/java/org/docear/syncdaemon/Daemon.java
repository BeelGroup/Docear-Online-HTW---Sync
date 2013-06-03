package org.docear.syncdaemon;

public class Daemon {

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
}
