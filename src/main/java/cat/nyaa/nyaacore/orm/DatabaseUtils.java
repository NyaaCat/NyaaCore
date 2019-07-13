package cat.nyaa.nyaacore.orm;

import cat.nyaa.nyaacore.orm.backends.BackendConfig;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.MysqlDatabase;
import cat.nyaa.nyaacore.orm.backends.SQLiteDatabase;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    /**
     * @param cfg
     * @return
     */
    public IConnectedDatabase connect(Plugin plugin, BackendConfig cfg) throws ClassNotFoundException, SQLException {
        if ("sqlite".equalsIgnoreCase(cfg.provider)) {
            return new SQLiteDatabase(newJdbcConnection(plugin, cfg));
        } else if ("mysql".equalsIgnoreCase(cfg.provider)) {
            return new MysqlDatabase(newJdbcConnection(plugin, cfg));
        } else {
            throw new IllegalArgumentException("Invalid provider: " + cfg.provider);
        }
    }


    /**
     * @param plugin
     * @param cfg
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    Connection newJdbcConnection(Plugin plugin, BackendConfig cfg) throws ClassNotFoundException, SQLException, IllegalArgumentException {
        String provider = cfg.provider;
        if ("sqlite".equalsIgnoreCase(provider)) {

            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + cfg.sqlite_file);

        } else if ("mysql".equalsIgnoreCase(provider)) {

            Class.forName(cfg.mysql_jdbc_driver);
            return DriverManager.getConnection(cfg.mysql_url, cfg.mysql_username, cfg.mysql_password);

        } else {
            throw new IllegalArgumentException("Invalid provider: " + provider);
        }
    }
}
