package org.docear.syncdaemon.indexdb.h2;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public enum Table {
    PROJECTS("projects", "id VARCHAR(500), rootpath VARCHAR(500), revision BIGINT, PRIMARY KEY (id)"),
    FILES("files", "path VARCHAR(500), " +
            "hash CHAR(128), " +
            "projectId VARCHAR(500), " +
            "isFolder BOOL, " +
            "isDeleted BOOL, " +
            "revision BIGINT," +
            "PRIMARY KEY (path, projectId)");

    private static final Logger logger = LoggerFactory.getLogger(Table.class);
    private final String name;
    private final String columns;

    private Table(String name, String columns) {
        this.name = name;
        this.columns = columns;
    }

    public void ensureCreated(final Connection conn) throws SQLException {
        Statement createTableStatement = null;
        try {
            createTableStatement = conn.createStatement();
            createTableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS " + name + "( " + columns + " )");
            logger.debug("table " + name + " exists");
        } finally {
            DbUtils.closeQuietly(createTableStatement);
        }
    }

    public static void ensureTablesCreated(Connection conn) throws SQLException {
        for (Table table : Table.values()) {
            table.ensureCreated(conn);
        }
    }

    public String getName() {
        return name;
    }
}
