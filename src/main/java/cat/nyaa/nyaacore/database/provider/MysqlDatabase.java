package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
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
        this.connection = newConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public Connection newConnection() {
        Connection conn;
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

    @Override
    public void recycleConnection(Connection conn) {
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        MysqlDatabase db = (MysqlDatabase) super.clone();
        db.connection = db.newConnection();
        return db;
    }
}
