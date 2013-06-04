package org.docear.syncdaemon;

public class DaemonThread extends Thread {
    private final Daemon daemon;

    public DaemonThread(Daemon daemon) {
        this.daemon = daemon;
        setDaemon(true);
    }

    @Override
    public void run() {
        daemon.onStart();
    }
}
