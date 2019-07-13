package cat.nyaa.nyaacore.orm.backends;

import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

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
    <T> ITable<T> getTable(Class<T> recordClass);

    @Override
    void close() throws SQLException;

    boolean verifySchema(String tableName, Class recordClass);


}
