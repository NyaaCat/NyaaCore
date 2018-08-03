package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import cat.nyaa.nyaacore.database.relational.TableStructure;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabase extends BaseDatabase {

    private Plugin plugin;
    private String file;
    private Connection dbConn;

    public SQLiteDatabase(Plugin basePlugin, String fileName) {
        file = fileName;
        plugin = basePlugin;
        dbConn = newConnection();
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
    final public Connection getConnection() {
        return dbConn;
    }

    @Override
    public Connection newConnection() {
        Connection conn;
        File dbFile = new File(plugin.getDataFolder(), file);
        try {
            Class.forName("org.sqlite.JDBC");
            String connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            plugin.getLogger().info("Connecting database: " + connStr);
            conn = DriverManager.getConnection(connStr);
            conn.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException(ex);
        }
        return conn;
    }

    @Override
    public void recycleConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Remember to close the new connection cloned.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        SQLiteDatabase db = (SQLiteDatabase) super.clone();
        db.dbConn = db.newConnection();
        return db;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void createTable(Class<?> cls) {
        Validate.notNull(cls);
        if (createdTableClasses.contains(cls)) return;
        TableStructure ts = TableStructure.fromClass(cls);
        String sql = ts.getCreateTableSQL("sqlite");
        try (Statement smt = getConnection().createStatement()){
            smt.executeUpdate(sql);
            createdTableClasses.add(cls);
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }
}
