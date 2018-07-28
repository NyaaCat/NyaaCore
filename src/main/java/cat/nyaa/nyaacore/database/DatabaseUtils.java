package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.keyvalue.KeyValueDB;
import cat.nyaa.nyaacore.database.provider.MapDatabase;
import cat.nyaa.nyaacore.database.provider.SQLiteDatabase;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import cat.nyaa.nyaacore.utils.ClassPathUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Database utils that provide database access according to plugin's configuration
 */
public class DatabaseUtils {
    public static <T> T getDatabae(Class<T> databaseType, JavaPlugin plugin, ConfigurationSection config) {
        if (databaseType == RelationalDB.class) {
            String type = config.getString("provider");
            if (type == null) throw new IllegalArgumentException("unknown relational db provider: null");
            if ("sqlite".equalsIgnoreCase(type)) {
                return (T) new SQLiteDatabase(plugin, config.getString("file"));
            } else {
                throw new IllegalArgumentException("unknown relational db provider: " + type);
            }
        } else if (databaseType == KeyValueDB.class) {
            String type = config.getString("provider");
            if (type == null) throw new IllegalArgumentException("unknown kv db provider: null");
            if ("map".equalsIgnoreCase(type)) {
                return (T) new MapDatabase<>();
            } else {
                throw new IllegalArgumentException("unknown kv db provider: " + type);
            }
        } else {
            throw new IllegalArgumentException("unexpected database type: " + databaseType.getName());
        }
    }
}
