package cat.nyaa.nyaacore.database.provider;

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
        return (T) new MysqlDatabase(plugin, jdbc == null ? "com.mysql.jdbc.Driver" : jdbc, (String) configuration.get("url"), (String) configuration.get("username"), (String) configuration.get("password"));
    }
}