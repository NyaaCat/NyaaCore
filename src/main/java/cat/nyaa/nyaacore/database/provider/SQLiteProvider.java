package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.DatabaseProvider;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import org.bukkit.plugin.Plugin;

import javax.persistence.Table;
import java.util.Map;

public class SQLiteProvider implements DatabaseProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Plugin plugin, Map<String, Object> configuration, Class<T> databaseType) {
        if (!databaseType.isAssignableFrom(SQLiteDatabase.class)) {
            throw new IllegalArgumentException();
        }
        return (T) new SQLiteDatabase(plugin, (String) configuration.get("file"));
    }
}