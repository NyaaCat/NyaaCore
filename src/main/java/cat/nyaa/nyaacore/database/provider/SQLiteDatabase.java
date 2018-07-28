package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import cat.nyaa.nyaacore.database.relational.TableStructure;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class SQLiteDatabase extends BaseDatabase {

    private Plugin plugin;
    private String file;
    private Connection dbConn;

    public SQLiteDatabase(Plugin basePlugin, String fileName) {
        file = fileName;
        plugin = basePlugin;
        dbConn = connect();
    }

    public Connection connect() {
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
        return connect();
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
        Object obj = super.clone();
        ((SQLiteDatabase) obj).connect();
        return obj;
    }

    @Override
    public void createTable(Class<?> cls) {
        Validate.notNull(cls);
        if (createdTableClasses.contains(cls)) return;
        TableStructure ts = TableStructure.fromClass(cls);
        String sql = ts.getCreateTableSQL("sqlite");
        try {
            Statement smt = getConnection().createStatement();
            smt.executeUpdate(sql);
            smt.close();
            createdTableClasses.add(cls);
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    /**
     * Execute a SQL file bundled with the plugin
     *
     * @param filename       full file name, including extension, in resources/sql folder
     * @param replacementMap {{key}} in the file will be replaced by value. Ignored if null. NOTE: sql injection will happen
     * @param cls            class of desired object
     * @param parameters     JDBC's positional parametrized query.
     * @return the result set, null if cls is null.
     */
    public <T> List<T> queryBundledAs(String filename, Map<String, String> replacementMap, Class<T> cls, Object... parameters) {
        String sql;
        try (
                InputStream inputStream = plugin.getResource("sql/" + filename);
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream()
        ) {
            int result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            sql = buf.toString(StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try (PreparedStatement stat = buildStatement(sql, replacementMap, parameters)) {
            boolean hasResult = stat.execute();
            if (cls == null) {
                return null;
            } else if (hasResult) {
                return parseResultSet(stat.getResultSet(), cls);
            } else {
                return new ArrayList<>();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void queryBundled(String filename, Map<String, String> replacementMap, Object... parameters) {
        queryBundledAs(filename, replacementMap, null, parameters);
    }
}
