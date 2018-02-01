package cat.nyaa.nyaacore.database;

import org.apache.commons.lang.NotImplementedException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlDatabase extends BaseDatabase implements RelationalDB {

    private String jdbcDriver;
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;
    private Class<?>[] classes;

    public MysqlDatabase(String jdbcDriver, String dbUrl, String user, String password, Class<?>[] classes){
        super(classes);
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
            connection = DriverManager.getConnection(dbUrl, user, password);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("connection failed", e);
        }
        createTables();
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
    public void updateTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteTable(Class<?> cls) {
        throw new NotImplementedException();
    }
}
