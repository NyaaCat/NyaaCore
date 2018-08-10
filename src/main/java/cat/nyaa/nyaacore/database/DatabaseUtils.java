package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.keyvalue.KeyValueDB;
import cat.nyaa.nyaacore.database.provider.DatabaseProvider;
import cat.nyaa.nyaacore.database.provider.MapProvider;
import cat.nyaa.nyaacore.database.provider.MysqlProvider;
import cat.nyaa.nyaacore.database.provider.SQLiteProvider;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sun.reflect.Reflection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Database utils that provide database access according to plugin's configuration
 */
public class DatabaseUtils {

    private DatabaseUtils() {
        throw new UnsupportedOperationException();
    }

    private static Map<String, DatabaseProvider> providerRegistry = new HashMap<>();

    /**
     * Register provider.
     *
     * @param name     provider name
     * @param provider provider instance
     */
    public static void registerProvider(String name, DatabaseProvider provider) {
        providerRegistry.put(name, provider);
    }

    public static boolean hasProvider(String name) {
        return providerRegistry.containsKey(name);
    }

    public static DatabaseProvider unregisterProvider(String name) {
        return providerRegistry.remove(name);
    }

    static {
        registerProvider("map", new MapProvider());
        registerProvider("sqlite", new SQLiteProvider());
        registerProvider("mysql", new MysqlProvider());
    }

    /**
     * Get database instance from provider and configuration specified
     *
     * @param <T>           generic type for return different subtype of {@link RelationalDB} or {@link KeyValueDB}
     * @param plugin        plugin requesting. may be null if provider
     * @param provider      provider name
     * @param configuration configuration
     * @return database instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String provider, Plugin plugin, Map<String, Object> configuration, Class<T> databaseType) {
        DatabaseProvider p = providerRegistry.get(provider);
        Validate.notNull(p, "Provider '" + provider + "' not found");
        T db = p.get(plugin, configuration, databaseType);
        Validate.notNull(db, "Provider '" + provider + "' returned null");
        return db;
    }

    /**
     * Get database instance from plugin's configuration section specified
     *
     * @param <T>         generic type for return different subtype of {@link RelationalDB} or {@link KeyValueDB}
     * @param plugin      plugin requesting. not null
     * @param sectionName configuration section in plugin's config
     * @return database instance
     */
    public static <T> T get(Plugin plugin, String sectionName, Class<T> databaseType) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(sectionName);
        Validate.notNull(section, "Please add a '" + sectionName + "' section containing a 'provider' value and (if provider requires) a 'connection' section to your " + plugin.getName() + "'s config file");
        ConfigurationSection conn = section.getConfigurationSection("connection");
        String provider = section.getString("provider");
        Validate.notNull(provider, "Please add a 'provider' value in 'database' section. Available: " + providerRegistry.keySet().stream().reduce("", (s, s2) -> s + ", " + s2));
        return get(provider, plugin, conn == null ? null : conn.getValues(false), databaseType);
    }

    /**
     * Get database instance from plugin's configuration section specified. Inferring plugin from callstack.
     *
     * @param <T>         generic type for return different subtype of {@link RelationalDB} or {@link KeyValueDB}
     * @param sectionName configuration section in plugin's config
     * @return database instance
     */
    public static <T> T get(String sectionName, Class<T> databaseType) {
        Objects.requireNonNull(databaseType);
        try {
            return get(JavaPlugin.getProvidingPlugin(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName())), sectionName, databaseType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get database instance from plugin's configuration section 'database'. Inferring plugin from callstack.
     *
     * @param <T> generic type for return different subtype of {@link RelationalDB} or {@link KeyValueDB}
     * @return database instance
     */
    public static <T> T get(Class<T> databaseType) {
        Objects.requireNonNull(databaseType);
        try {
            return get(JavaPlugin.getProvidingPlugin(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName())), "database", databaseType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
