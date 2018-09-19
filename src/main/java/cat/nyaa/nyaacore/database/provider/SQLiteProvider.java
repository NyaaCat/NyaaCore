package cat.nyaa.nyaacore.database.provider;

import org.bukkit.plugin.Plugin;

import java.util.Map;

public class SQLiteProvider implements DatabaseProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Plugin plugin, Map<String, Object> configuration, Class<T> databaseType) {
        if (!databaseType.isAssignableFrom(SQLiteDatabase.class)) {
            throw new IllegalArgumentException();
        }
        String file = (String) configuration.get("file");
        if (file == null) {
            file = plugin.getName() + ".db";
        }
        return (T) new SQLiteDatabase(plugin, file);
    }
}