package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MysqlDatabase extends BaseDatabase {

    private final Plugin plugin;
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;

    public MysqlDatabase(Plugin basePlugin, String jdbcDriver, String dbUrl, String user, String password) {
        super(basePlugin.getLogger(), (runnable) -> Bukkit.getScheduler().runTaskAsynchronously(basePlugin, runnable));
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

    public MysqlDatabase(Plugin basePlugin, String jdbcDriver, String dbUrl, String user, String password, Consumer<Runnable> executor, Logger logger) {
        super(logger, executor);
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
            throw new RuntimeException("connection failed", e);
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
}
