package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import cat.nyaa.nyaacore.database.relational.SynchronizedQuery;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class MysqlDatabase extends BaseDatabase {

    private final Plugin plugin;
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;
    public static Function<Plugin, Consumer<Runnable>> executor = (plugin) -> (runnable) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    public static Function<Plugin, Logger> logger = Plugin::getLogger;

    public MysqlDatabase(Plugin basePlugin, String jdbcDriver, String dbUrl, String user, String password) {
        super(logger.apply(basePlugin), (runnable) -> executor.apply(basePlugin));
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Jdbc Driver not available", e);
        }
        this.plugin = basePlugin;
        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;
        this.connection = createConnection();
        executeAsync(this::fillPool);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public Connection createConnection() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(dbUrl, user, password);
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("connection failed: " + user + "@" + dbUrl, e);
        }
        return conn;
    }

    @Override
    public void close() {
        super.close();
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        MysqlDatabase db = (MysqlDatabase) super.clone();
        db.connection = db.createConnection();
        db.connectionPool.clear();
        db.usedConnections.clear();
        db.fillPool();
        return db;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public <T> SynchronizedQuery.TransactionalQuery<T> queryTransactional(Class<T> tableClass) {
        createTable(tableClass);
        Connection conn = newConnection();
        int oldTransactionIsolation;
        try {
            oldTransactionIsolation = conn.getTransactionIsolation();
            conn.setTransactionIsolation(Math.max(oldTransactionIsolation, Connection.TRANSACTION_READ_COMMITTED));
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return new SynchronizedQuery.TransactionalQuery<T>(tableClass, conn) {
            @Override
            public T selectUniqueForUpdate() {
                String sql = "SELECT " + table.getColumnNamesString() + " FROM " + table.tableName;
                List<Object> objects = new ArrayList<>();
                sql = buildWhereClause(sql, objects);
                sql += " LIMIT 1 FOR UPDATE";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    int x = 1;
                    for (Object obj : objects) {
                        stmt.setObject(x, obj);
                        x++;
                    }
                    T result = null;
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            result = table.getObjectFromResultSet(rs);
                        }
                    }
                    return result;
                } catch (SQLException | ReflectiveOperationException ex) {
                    throw new RuntimeException(sql, ex);
                }
            }

            @Override
            public void close() {
                super.close();
                try {
                    conn.setAutoCommit(true);
                    conn.setTransactionIsolation(oldTransactionIsolation);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    recycleConnection(conn);
                }
            }
        };
    }
}
