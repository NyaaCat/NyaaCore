package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.RelationalDB;
import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlDatabase extends BaseDatabase {

    private final Plugin plugin;
    private String jdbcDriver;
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;

    public MysqlDatabase(Plugin basePlugin, String jdbcDriver, String dbUrl, String user, String password){
        this.plugin = basePlugin;
        this.jdbcDriver = jdbcDriver;
        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;
        this.connection = connect();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T connect() {
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Jdbc Driver not available", e);
        }
        try {
            plugin.getLogger().info("Connecting database " + dbUrl + " as " + user);
            conn = DriverManager.getConnection(dbUrl, user, password);
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("connection failed", e);
        }
        return conn;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public Class<?>[] getTables() {
        return classes;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    protected Connection newConnection() {
        try {
            return DriverManager.getConnection(dbUrl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void recycleConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
    }
}
