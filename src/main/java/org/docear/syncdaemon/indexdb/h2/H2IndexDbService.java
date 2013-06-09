package org.docear.syncdaemon.indexdb.h2;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.PersistenceException;

import java.sql.*;

public class H2IndexDbService implements IndexDbService {

    public H2IndexDbService() {
    }

    @Override
    public void save(final FileMetaData currentServerMetaData) throws PersistenceException {
        execute(new WithConnection() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                try {
                    statement = connection.prepareStatement("MERGE INTO " + Table.FILES.getName() + " VALUES (?, ?, ?, ?, ?, ?)");
                    statement.setString(1, currentServerMetaData.getPath());
                    statement.setString(2, currentServerMetaData.getHash());
                    statement.setString(3, currentServerMetaData.getProjectId());
                    statement.setBoolean(4, currentServerMetaData.isFolder());
                    statement.setBoolean(5, currentServerMetaData.isDeleted());
                    statement.setLong(6, currentServerMetaData.getRevision());
                    statement.execute();
                    return null;//not needed
                } finally {
                    DbUtils.closeQuietly(statement);
                }

            }
        });
    }

    @Override
    public long getProjectRevision(final String projectId) throws PersistenceException {
        return (Long) execute(new WithConnection() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                Long result = null;
                try {
                    statement = connection.prepareStatement("SELECT revision FROM " + Table.PROJECTS.getName() + " WHERE id = ?");
                    statement.setString(1, projectId);
                    resultSet = statement.executeQuery();
                    final boolean hasResult = resultSet.next();
                    if (hasResult) {
                        result = resultSet.getLong(1);
                    }
                    return result;
                } finally {
                    DbUtils.closeQuietly(resultSet);
                    DbUtils.closeQuietly(statement);
                }
            }
        });
    }

    @Override
    public void setProjectRevision(final String projectId, final long revision) throws PersistenceException {
        execute(new WithConnection() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                try {
                    statement = connection.prepareStatement("MERGE INTO " + Table.PROJECTS.getName() + " (id, revision) KEY(id) VALUES (?, ?)");
                    statement.setString(1, projectId);
                    statement.setLong(2, revision);
                    statement.execute();
                    return null;//not needed
                } finally {
                    DbUtils.closeQuietly(statement);
                }

            }
        });
    }

    @Override
    public FileMetaData getFileMetaData(final FileMetaData fileMetaData) throws PersistenceException {
        return (FileMetaData) execute(new WithConnection() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                FileMetaData result = null;
                try {
                    statement = connection.prepareStatement("SELECT * FROM " + Table.FILES.getName() + " WHERE path = ? and projectId = ?");
                    statement.setString(1, fileMetaData.getPath());
                    statement.setString(2, fileMetaData.getProjectId());
                    resultSet = statement.executeQuery();
                    final boolean hasResult = resultSet.next();
                    if (hasResult) {
                        final String path = resultSet.getString("path");
                        final String hash = resultSet.getString("hash");
                        final String projectId = resultSet.getString("projectId");
                        final boolean folder = resultSet.getBoolean("isFolder");
                        final boolean deleted = resultSet.getBoolean("isDeleted");
                        final long revision = resultSet.getLong("revision");
                        result = new FileMetaData(path, hash, projectId, folder, deleted, revision);
                    }
                    return result;
                } finally {
                    DbUtils.closeQuietly(resultSet);
                    DbUtils.closeQuietly(statement);
                }
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:docearsync", "", "");//TODO as file database in prod, in test in memory
    }

    //TODO use generics
    private static interface WithConnection {
        Object execute(final Connection connection) throws SQLException;
    }

    private Object execute(final WithConnection withConnection) throws PersistenceException {
        Connection connection = null;
         try {
             connection = getConnection();
             return withConnection.execute(connection);
         } catch (SQLException e) {
             throw new PersistenceException(e);
         } finally {
             DbUtils.closeQuietly(connection);
         }
    }
}
