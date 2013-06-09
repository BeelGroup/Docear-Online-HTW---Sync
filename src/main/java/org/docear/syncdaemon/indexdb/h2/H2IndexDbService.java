package org.docear.syncdaemon.indexdb.h2;

import org.apache.commons.dbutils.DbUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.PersistenceException;

import java.sql.*;

public class H2IndexDbService implements IndexDbService {

    public H2IndexDbService() {
    }

    @Override
    public void save(final FileMetaData currentServerMetaData) throws PersistenceException {
        execute(new WithStatement<Object>() {
            @Override
            public String sql() {
                return "MERGE INTO " + Table.FILES.getName() + " VALUES (?, ?, ?, ?, ?, ?)";
            }

            @Override
            public void statementPreparation(PreparedStatement statement) throws SQLException {
                statement.setString(1, currentServerMetaData.getPath());
                statement.setString(2, currentServerMetaData.getHash());
                statement.setString(3, currentServerMetaData.getProjectId());
                statement.setBoolean(4, currentServerMetaData.isFolder());
                statement.setBoolean(5, currentServerMetaData.isDeleted());
                statement.setLong(6, currentServerMetaData.getRevision());
            }
        });
    }

    @Override
    public long getProjectRevision(final String projectId) throws PersistenceException {
        return execute(new WithQuery<Long>() {
            @Override
            public String sql() {
                return "SELECT revision FROM " + Table.PROJECTS.getName() + " WHERE id = ?";
            }

            @Override
            public void statementPreparation(PreparedStatement statement) throws SQLException {
                statement.setString(1, projectId);
            }

            @Override
            public Long extractResult(ResultSet resultSet) throws SQLException {
                return resultSet.getLong(1);
            }
        });
    }

    @Override
    public void setProjectRevision(final String projectId, final long revision) throws PersistenceException {
        execute(new WithStatement<Object>() {
            @Override
            public String sql() {
                return "MERGE INTO " + Table.PROJECTS.getName() + " (id, revision) KEY(id) VALUES (?, ?)";
            }

            @Override
            public void statementPreparation(PreparedStatement statement) throws SQLException {
                statement.setString(1, projectId);
                statement.setLong(2, revision);
            }
        });
    }

    @Override
    public FileMetaData getFileMetaData(final FileMetaData fileMetaData) throws PersistenceException {
        return execute(new WithQuery<FileMetaData>() {
            @Override
            public String sql() {
                return "SELECT * FROM " + Table.FILES.getName() + " WHERE path = ? and projectId = ?";
            }

            @Override
            public void statementPreparation(PreparedStatement statement) throws SQLException {
                statement.setString(1, fileMetaData.getPath());
                statement.setString(2, fileMetaData.getProjectId());
            }

            @Override
            public FileMetaData extractResult(ResultSet resultSet) throws SQLException {
                final String path = resultSet.getString("path");
                final String hash = resultSet.getString("hash");
                final String projectId = resultSet.getString("projectId");
                final boolean folder = resultSet.getBoolean("isFolder");
                final boolean deleted = resultSet.getBoolean("isDeleted");
                final long revision = resultSet.getLong("revision");
               return new FileMetaData(path, hash, projectId, folder, deleted, revision);
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:docearsync", "", "");//TODO as file database in prod, in test in memory
    }

    private static interface WithConnection<T> {
        T execute(final Connection connection) throws SQLException;
    }

    private static interface WithStatement<T> {
        String sql();
        void statementPreparation(PreparedStatement statement) throws SQLException;
    }

    private static interface WithQuery<T> extends WithStatement {
        T extractResult(ResultSet resultSet) throws SQLException;
    }

    private <T> T execute(final WithConnection<T> withConnection) throws PersistenceException {
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

    private <T> T execute(final WithQuery<T> withQuery) throws PersistenceException {
        return execute(new WithConnection<T>() {
            @Override
            public T execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                T result = null;
                try {
                    statement = connection.prepareStatement(withQuery.sql());
                    withQuery.statementPreparation(statement);
                    resultSet = statement.executeQuery();
                    final boolean hasResult = resultSet.next();
                    if (hasResult) {
                        result = withQuery.extractResult(resultSet);
                    }
                    return result;
                } finally {
                    DbUtils.closeQuietly(resultSet);
                    DbUtils.closeQuietly(statement);
                }
            }
        });
    }

    private void execute(final WithStatement withStatement) throws PersistenceException {
        execute(new WithConnection<Object>() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = connection.prepareStatement(withStatement.sql());
                    withStatement.statementPreparation(preparedStatement);
                    preparedStatement.execute();
                    return null;
                } finally {
                    DbUtils.closeQuietly(preparedStatement);
                }
            }
        });
    }
}
