package cat.nyaa.nyaacore.orm.backends;

import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * IDatabase contains an internal JDBC connection. see {@link cat.nyaa.nyaacore.orm.DatabaseUtils#connect(Plugin, BackendConfig)}
 * So the user *MUST* disconnect this
 * <p>
 * This interface is *NOT* thread safe.
 * If you what multiple connections, call {@link cat.nyaa.nyaacore.orm.DatabaseUtils#connect(Plugin, BackendConfig)} to create more connections.
 * AND **NEVER** FORGET TO {@link IConnectedDatabase#close()}
 * <p>
 * All ITable returned by {@link IConnectedDatabase#getTable(Class)} share this same connection.
 * NEVER operate on different tables simultaneously.
 * <p>
 * General rule:
 * when in double, use {@link cat.nyaa.nyaacore.orm.DatabaseUtils#connect(Plugin, BackendConfig)} to get a new connection
 */
public interface IConnectedDatabase extends AutoCloseable {
    /**
     * Get underlying JDBC connection. Easily gets messed up. Avoid if you can.
     *
     * @return
     */
    Connection getConnection();

    <T> ITypedTable<T> getTable(Class<T> recordClass);

    /**
     * @param recordClass
     * @param <T>
     * @return
     */
    <T> ITypedTable<T> getUnverifiedTable(Class<T> recordClass);

    @Override
    void close() throws SQLException;

    boolean verifySchema(String tableName, Class recordClass);

    /**
     * Execute a SQL file bundled with some plugin, using the default Connection.
     *
     * @param filename       full file name, including extension, in resources/sql folder
     * @param replacementMap {{key}} in the file will be replaced by value. Ignored if null. NOTE: sql injection will happen
     * @param cls            class of desired object
     * @param parameters     JDBC's positional parametrized query. Java Type
     * @return the result set, null if cls is null.
     */
    <T> List<T> queryBundledAs(Plugin plugin, String filename, Map<String, String> replacementMap, Class<T> cls, Object... parameters);
}
