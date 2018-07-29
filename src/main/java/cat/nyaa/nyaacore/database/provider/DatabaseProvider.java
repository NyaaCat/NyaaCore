package cat.nyaa.nyaacore.database.provider;

import org.bukkit.plugin.Plugin;

import java.util.Map;

public interface DatabaseProvider {
    <T> T get(Plugin plugin, Map<String, Object> configuration, Class<T> databaseType);
}
