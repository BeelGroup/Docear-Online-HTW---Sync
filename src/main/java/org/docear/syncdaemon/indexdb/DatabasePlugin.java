package org.docear.syncdaemon.indexdb;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabasePlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePlugin.class);
    private Server server;

    public DatabasePlugin(Daemon daemon) {
        super(daemon);
    }

    @Override
    public void onStart() {
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:docearsync", "", "");
            logger.debug("started H2 database");
            if (shouldStartDebugWebserver()) {
                Server.startWebServer(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldStartDebugWebserver() {
        return daemon().getConfig().getBoolean("indexdb.h2browser");
    }

    @Override
    public void onStop() {
        if (server != null) {
            logger.debug("stopping H2 database");
            server.shutdown();
            server = null;
        }
    }
}
