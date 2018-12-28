package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import cat.nyaa.nyaacore.database.relational.SynchronizedQuery;
import cat.nyaa.nyaacore.database.relational.TableStructure;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class SQLiteDatabase extends BaseDatabase {

    private Plugin plugin;
    private String file;
    private Connection dbConn;
    public static Function<Plugin, Consumer<Runnable>> executor = (plugin) -> (runnable) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    public static Function<Plugin, Logger> logger = Plugin::getLogger;

    public SQLiteDatabase(Plugin basePlugin, String fileName) {
        super(logger.apply(basePlugin), executor.apply(basePlugin));
        file = fileName;
        plugin = basePlugin;
        dbConn = createConnection();
        executeAsync(this::fillPool);
    }

    @Override
    public void close() {
        super.close();
        try {
            dbConn.close();
            dbConn = null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public Connection getConnection() {
        return dbConn;
    }

    @Override
    public Connection createConnection() {
        Connection conn;
        File dbFile = new File(plugin.getDataFolder(), file);
        try {
            Class.forName("org.sqlite.JDBC");
            String connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            conn = DriverManager.getConnection(connStr);
            conn.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException(ex);
        }
        return conn;
    }

    /**
     * Remember to close the new connection cloned.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        SQLiteDatabase db = (SQLiteDatabase) super.clone();
        db.dbConn = db.createConnection();
        db.connectionPool.clear();
        db.usedConnections.clear();
        db.fillPool();
        return db;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void createTable(Class<?> cls) {
        Validate.notNull(cls);
        if (createdTableClasses.contains(cls)) return;
        TableStructure ts = TableStructure.fromClass(cls);
        String sql = ts.getCreateTableSQL("sqlite");
        try (Statement smt = getConnection().createStatement()) {
            smt.executeUpdate(sql);
            createdTableClasses.add(cls);
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public <T> SynchronizedQuery.TransactionalQuery<T> queryTransactional(Class<T> tableClass) {
        createTable(tableClass);
        Connection conn = newConnection();
        try {
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return new SynchronizedQuery.TransactionalQuery<T>(tableClass, conn) {
            @Override
            public T selectUniqueForUpdate() {
                return selectUniqueUnchecked();
            }

            @Override
            public void close() {
                super.close();
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    recycleConnection(conn);
                }
            }
        };
    }
}
