package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.Database;
import cat.nyaa.nyaacore.database.RelationalDB;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class SQLiteDatabase extends BaseDatabase implements Cloneable, RelationalDB {

    private Plugin plugin;

    private String file;

    private Class<?>[] classes;

    SQLiteDatabase(Plugin basePlugin, String fileName, Class<?>[] tableClasses) {
        super(tableClasses);
        file = fileName;
        plugin = basePlugin;
        classes = tableClasses;
    }

    private Connection dbConn;

    @SuppressWarnings("unchecked")
    public <T extends Database> T connect() {
        File dbFile = new File(plugin.getDataFolder(), file);
        try {
            Class.forName("org.sqlite.JDBC");
            String connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            plugin.getLogger().info("Connecting database: " + connStr);
            dbConn = DriverManager.getConnection(connStr);
            dbConn.setAutoCommit(true);
            createTables(true);
        } catch (ClassNotFoundException | SQLException ex) {
            dbConn = null;
            throw new RuntimeException(ex);
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
    protected Class<?>[] getTables() {
        return classes;
    }

    @Override
    final protected Connection getConnection() {
        return dbConn;
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
    public void createTable(Class<?> cls) {
        createTable(cls, true);
    }

    @Override
    public void updateTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    @Override
    public void enableAutoCommit() {
        try {
            dbConn.commit();
            dbConn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disableAutoCommit() {
        try {
            dbConn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
