package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLiteDatabase extends BaseDatabase {

    private Class<?>[] tableClasses = new Class<?>[]{};
    private Plugin plugin;
    private String file;
    private String connStr;

    public SQLiteDatabase(Plugin basePlugin, String fileName) {
        file = fileName;
        plugin = basePlugin;
    }

    public SQLiteDatabase(Plugin basePlugin, String fileName, Class<?>[] tableClasses) {
        this(basePlugin, fileName);
        this.tableClasses = tableClasses;
    }

    private Connection dbConn;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T connect() {
        File dbFile = new File(plugin.getDataFolder(), file);
        try {
            Class.forName("org.sqlite.JDBC");
            connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            plugin.getLogger().info("Connecting database: " + connStr);
            dbConn = DriverManager.getConnection(connStr);
            dbConn.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException ex) {
            dbConn = null;
            throw new RuntimeException(ex);
        }
        for (Class<?> c : tableClasses) {
            createTable(c);
        }
        return (T) this;
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
    public Connection getConnection() {
        return dbConn;
    }

    @Override
    protected Connection newConnection() {
        try {
            return DriverManager.getConnection(connStr);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
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

    /**
     * Remember to close the new connection cloned.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Object obj = super.clone();
        ((SQLiteDatabase) obj).connect();
        return obj;
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

    @Override
    public void commitTransaction() {
        try {
            dbConn.commit();
            dbConn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginTransaction() {
        try {
            dbConn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
            dbConn.rollback();
            dbConn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
