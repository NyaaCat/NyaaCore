package cat.nyaa.nyaacore.database;

import org.bukkit.plugin.Plugin;

import java.util.Map;

public interface DatabaseProvider {
    Database get(Plugin plugin, Map<String, Object> configuration);
}
