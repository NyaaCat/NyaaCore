package cat.nyaa.nyaacore.database;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Database utils that provide database access according to plugin's configuration
 */
public class DatabaseUtils {
    private static Map<String, DatabaseProvider> providerRegistration = new HashMap<>();

    /**
     * Register provider.
     *
     * @param name     provider name
     * @param provider provider instance
     */
    public static void registerProvider(String name, DatabaseProvider provider){
        providerRegistration.put(name, provider);
    }

    static {
        registerProvider("map", new MapProvider());
        registerProvider("sqlite", new SQLiteProvider());
    }

    /**
     * Get database instance from provider and configuration specified
     *
     * @param <T>           generic type for return different subtype of {@link Database}}
     * @param plugin        plugin requesting. may be null if provider
     * @param provider      provider name
     * @param configuration configuration
     * @return database instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends Database> T get(String provider, JavaPlugin plugin, Map<String, Object> configuration){
        DatabaseProvider p = providerRegistration.get(provider);
        Validate.notNull(p, "Provider '" + provider + "' not found");
        Database db = p.get(plugin, configuration);
        Validate.notNull(db, "Provider '" + provider + "' returned null");
        return (T) db;
    }

    /**
     * Get database instance from plugin's configuration section specified
     *
     * @param <T>           generic type for return different subtype of {@link Database}}
     * @param plugin        plugin requesting. not null
     * @param sectionName   configuration section in plugin's config
     * @return database instance
     */
    public static <T extends Database> T get(JavaPlugin plugin, String sectionName){
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(sectionName);
        Validate.notNull(section, "Please add a 'database' section containing a 'provider' value and (if provider requires) a 'connection' section");
        ConfigurationSection conn = section.getConfigurationSection("connection");
        String provider = section.getString("provider");
        Validate.notNull(provider, "Please add a 'provider' value in 'database' section. Available: " + providerRegistration.keySet().stream().reduce("", (s, s2) -> s + ", " + s2));
        return get(provider, plugin, conn == null ? null : conn.getValues(false));
    }

    /**
     * Get database instance from plugin's configuration section specified. Inferring plugin from callstack.
     *
     * @param <T>           generic type for return different subtype of {@link Database}}
     * @param sectionName   configuration section in plugin's config
     * @return database instance
     */
    public static <T extends Database> T get(String sectionName){
        try {
            return get(JavaPlugin.getProvidingPlugin(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName())), sectionName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Get database instance from plugin's configuration section 'database'. Inferring plugin from callstack.
     *
     * @param <T>           generic type for return different subtype of {@link Database}}
     * @return database instance
     */
    public static <T extends Database> T get(){
        try {
            return get(JavaPlugin.getProvidingPlugin(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName())), "database");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException();
        }
    }
}
