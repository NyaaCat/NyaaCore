package cat.nyaa.nyaacore.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestSQLiteDatabase extends BaseDatabase {
    public final File dbFile = new File("./test.db");
    protected TestSQLiteDatabase() {
        super();
        connect();
    }

    @Override
    protected Class<?>[] getTables() {
        return new Class[]{TestTable.class};
    }

    protected Connection dbConn;

    protected void connect() {
        dbFile.delete();
        System.out.print("dbPath: " + dbFile.getAbsolutePath() + "\n");
        try {
            Class.forName("org.sqlite.JDBC");
            String connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            //getPlugin().getLogger().info("Connecting database: " + connStr);
            dbConn = DriverManager.getConnection(connStr);
            dbConn.setAutoCommit(true);
            createTables();
        } catch (ClassNotFoundException | SQLException ex) {
            dbConn = null;
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        try {
            dbConn.close();
            dbConn = null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final protected Connection getConnection() {
        return dbConn;
    }
}
