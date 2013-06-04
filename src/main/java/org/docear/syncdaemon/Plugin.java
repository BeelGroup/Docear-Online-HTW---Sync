package org.docear.syncdaemon;

public abstract class Plugin {
	
	private final Daemon daemon;

	public Plugin(Daemon daemon) {
		this.daemon = daemon;
	}

	protected Daemon daemon() {
		return daemon;
	}
	
	public boolean enabled() {
		return true;
	}
	
	public abstract void onStart();
	public abstract void onStop();
}
