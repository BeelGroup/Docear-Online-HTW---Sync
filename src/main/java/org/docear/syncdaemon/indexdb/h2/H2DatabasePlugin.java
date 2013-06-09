package org.docear.syncdaemon.indexdb.h2;

import org.apache.commons.dbutils.DbUtils;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.Plugin;
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
        Connection conn = null;
        try {
            Class.forName("org.h2.Driver");
            final H2IndexDbService service = (H2IndexDbService) daemon().service(IndexDbService.class);
            keepAliveConnection = service.getConnection();
            conn = service.getConnection();
            createTables(conn);
            logger.debug("started H2 database");
            if (shouldStartDebugWebServer()) {
                Server.startWebServer(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    private void createTables(Connection conn) throws SQLException {
        createTable(conn, "files", "path VARCHAR(500), " +
                "hash CHAR(128), " +
                "projectId VARCHAR(500), " +
                "isFolder BOOL, " +
                "isDeleted BOOL, " +
                "revision BIGINT," +
                "PRIMARY KEY (path, projectId)");
        createTable(conn, "projects", "id VARCHAR(500), revision BIGINT, PRIMARY KEY (id)");
    }

    private void createTable(Connection conn, String name, String columns) throws SQLException {
        Statement createTableStatement = null;
        try {
            createTableStatement = conn.createStatement();
            createTableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS " + name + "( " + columns + " )");
            logger.debug("table " + name + " exists");
        } finally {
            DbUtils.closeQuietly(createTableStatement);
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
