package cat.nyaa.nyaacore.database;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class DatabaseUtils {
    private static Map<String, DatabaseProvider> providerRegistration = new HashMap<>();

    public static void registerProvider(String name, DatabaseProvider provider){
        providerRegistration.put(name, provider);
    }

    static {
        registerProvider("map", new MapProvider());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Database> T get(JavaPlugin plugin, String provider, Map<String, Object> configuration){
        DatabaseProvider p = providerRegistration.get(provider);
        Validate.notNull(p, "Provider '" + provider + "' not found");
        Database db = p.get(plugin, configuration);
        return (T) db;
    }

    public static <T extends Database> T get(JavaPlugin plugin, String sectionName){
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(sectionName);
        return get(plugin, section.getString("provider"), section.getConfigurationSection("connection").getValues(false));
    }

    public static <T extends Database> T get(String sectionName){
        try {
            return get(JavaPlugin.getProvidingPlugin(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName())), sectionName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException();
        }
    }

    public static <T extends Database> T get(){
        try {
            return get(JavaPlugin.getProvidingPlugin(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName())), "database");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException();
        }
    }
}
