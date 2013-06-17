package org.docear.syncdaemon.indexdb.h2;

import org.apache.commons.dbutils.DbUtils;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
import org.docear.syncdaemon.config.ConfigService;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Plugin that is activated when the service H2IndexDbService is used.
 */
public class H2DatabasePlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(H2DatabasePlugin.class);
    private Server server;
    private Connection keepAliveConnection;//if H2 is used as in memory database it deletes the database if no connection is open

    public H2DatabasePlugin(Daemon daemon) {
        super(daemon);
    }

    @Override
    public boolean enabled() {
        return daemon().service(IndexDbService.class) instanceof H2IndexDbService;
    }

    @Override
    public void onStart() {
        logger.info("starting H2 plugin");
        final String indexDbPath = daemon().service(ConfigService.class).getSyncDaemonHome() + "/db/index";
        final String connectionUrl = "jdbc:h2:" + indexDbPath;
        getService().setConnectionUrl(connectionUrl);
        loadH2Driver();
        Connection conn = null;
        try {
            keepAliveConnection = getConnection();
            conn = getConnection();
            Table.ensureTablesCreated(conn);
            if (shouldStartDebugWebServer()) {
                Server.startWebServer(getConnection());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    private Connection getConnection() throws SQLException {
        return getService().getConnection();
    }

    private H2IndexDbService getService() {
        return (H2IndexDbService) daemon().service(IndexDbService.class);
    }

    private void loadH2Driver() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldStartDebugWebServer() {
        return daemon().getConfig().getBoolean("indexdb.h2browser");
    }

    @Override
    public void onStop() {
        DbUtils.closeQuietly(keepAliveConnection);
        if (server != null) {
            logger.debug("stopping H2 database");
            server.shutdown();
            server = null;
        }
    }
}
