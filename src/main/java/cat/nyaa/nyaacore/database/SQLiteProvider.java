package cat.nyaa.nyaacore.database;

import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Map;

public class SQLiteProvider implements DatabaseProvider {
    @Override
    @SuppressWarnings("unchecked")
    public SQLiteDatabase get(Plugin plugin, Map<String, Object> configuration) {
        Object obj = configuration.get("tables");
        if(!(obj instanceof Collection)){
            throw new IllegalArgumentException();
        }
        Collection<String> tables = ((Collection<String>) configuration.get("tables"));
        Class<?>[] classes = tables.stream().map(s -> {
            try {
                return Class.forName(s);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException();
            }
        }).toArray(Class<?>[]::new);
        return new SQLiteDatabase(plugin, (String)configuration.get("file"), classes);
    }
}
