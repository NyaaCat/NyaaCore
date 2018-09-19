package cat.nyaa.nyaacore.database.provider;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import java.util.Map;

@SuppressWarnings("unchecked")
public class MysqlProvider implements DatabaseProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Plugin plugin, Map<String, Object> configuration, Class<T> databaseType) {
        if (!databaseType.isAssignableFrom(MysqlDatabase.class)) {
            throw new IllegalArgumentException();
        }
        String jdbc = (String) configuration.get("jdbc");
        String url = (String) configuration.get("url");
        Validate.notNull(url, "Please specify 'url' for MySQL.");
        String username = (String) configuration.get("username");
        String password = (String) configuration.get("password");

        return (T) new MysqlDatabase(plugin, jdbc == null ? "com.mysql.jdbc.Driver" : jdbc, url, username, password);
    }
}