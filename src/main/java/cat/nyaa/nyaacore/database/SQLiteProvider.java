package cat.nyaa.nyaacore.database;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class SQLiteProvider implements DatabaseProvider {
    @Override
    public SQLiteDatabase get(Plugin plugin, Map<String, Object> configuration) {
        Class<?>[] classes = DatabaseUtils.scanClasses((JavaPlugin) plugin, configuration);
        return new SQLiteDatabase(plugin, (String)configuration.get("file"), classes);
    }
}