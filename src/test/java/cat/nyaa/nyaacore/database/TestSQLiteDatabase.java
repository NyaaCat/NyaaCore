package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestSQLiteDatabase extends BaseDatabase {
    public final File dbFile = new File("./test.db");

    protected TestSQLiteDatabase() {
        connect();
    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void rollbackTransaction() {

    }

    @Override
    public void commitTransaction() {

    }

    @Override
    public Class<?>[] getTables() {
        return new Class[]{TestTable.class};
    }

    protected Connection dbConn;

    public TestSQLiteDatabase connect() {
        dbFile.delete();
        System.out.print("dbPath: " + dbFile.getAbsolutePath() + "\n");
        try {
            Class.forName("org.sqlite.JDBC");
            String connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            //getPlugin().getLogger().info("Connecting database: " + connStr);
            dbConn = DriverManager.getConnection(connStr);
            dbConn.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException ex) {
            dbConn = null;
            throw new RuntimeException(ex);
        }
        return this;
    }

    public void close() {
        try {
            dbConn.close();
            dbConn = null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final Connection getConnection() {
        return dbConn;
    }

    @Override
    protected Connection newConnection() {
        return null;
    }

    @Override
    protected void recycleConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
