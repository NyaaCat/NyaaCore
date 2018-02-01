package cat.nyaa.nyaacore.database;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlDatabase extends BaseDatabase implements RelationalDB {

    private final Plugin plugin;
    private String jdbcDriver;
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;
    private Class<?>[] classes;

    public MysqlDatabase(Plugin basePlugin, String jdbcDriver, String dbUrl, String user, String password, Class<?>[] classes){
        super(classes);
        this.plugin = basePlugin;
        this.jdbcDriver = jdbcDriver;
        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;
        this.classes = classes;
    }

    @Override
    public void connect() {
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Jdbc Driver not available", e);
        }
        try {
            plugin.getLogger().info("Connecting database " + dbUrl + " as " + user);
            connection = DriverManager.getConnection(dbUrl, user, password);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("connection failed", e);
        }
        createTables(false);
    }

    @Override
    protected Class<?>[] getTables() {
        return classes;
    }

    @Override
    protected Connection getConnection() {
        return connection;
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
    public void createTable(Class<?> cls) {
        createTable(cls, false);
    }

    @Override
    public void updateTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteTable(Class<?> cls) {
        throw new NotImplementedException();
    }
}
