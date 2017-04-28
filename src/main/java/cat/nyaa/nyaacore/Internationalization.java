package cat.nyaa.nyaacore;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.librazy.nyaautils_lang_checker.LangKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Map;

public abstract class Internationalization {
    /**
     * Use English as default & fallback language
     */
    private static final String DEFAULT_LANGUAGE = "en_US";
    /**
     * Internal language map is loaded and should only be loaded by {@link NyaaCoreLoader#onLoad()}
     * Once it's loaded, it cannot be reloaded or modified.
     * The internal map will be shared across all plugins using {@link Internationalization}
     */
    private final static Map<String, String> internalMap = new HashMap<>();
    /**
     * Per-plugin language map used by {@link Internationalization}
     * This map has a higher priority than {@link this#internalMap}
     * So it's possible to overwrite some internal language keys here.
     */
    private final Map<String, String> map = new HashMap<>();

    /**
     * Dependent plugins should provide the "main" class instance
     * So that language files could be loaded automatically
     *
     * @return the plugin class instance
     */
    protected abstract JavaPlugin getPlugin();

    /**
     * @return the language to be loaded into {@link this#map}
     */
    protected abstract String getLanguage();


    /**
     * Reset then load per-plugin language map
     * Based on {@link this#getPlugin()} and {@link this#getLanguage()}
     */
    public void load() {
        map.clear();
        String language = getLanguage();
        JavaPlugin plugin = getPlugin();
        if (language == null) language = DEFAULT_LANGUAGE;
        // language map
        loadLanguageMap(plugin, language);
        // save (probably) modified language file back to disk
        saveLanguageMap();

        plugin.getLogger().info(get("internal.info.using_language", language));
    }

    /**
     * Load per-plugin language map
     * Based on {@link this#getPlugin()} and {@link this#getLanguage()}
     */
    public void saveLanguageMap() {
        JavaPlugin plugin = getPlugin();
        String language = getLanguage();
        File localLangFile = new File(plugin.getDataFolder(), language + ".yml");
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            for (String key : map.keySet()) {
                yaml.set(key, map.get(key));
            }
            yaml.save(localLangFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Cannot save language file: " + language + ".yml");
        }
    }

    private void loadLanguageMap(JavaPlugin plugin, String language) {
        map.clear();
        boolean forceJar = System.getProperty("nyaautils.i18n.loadFromDisk", "true").equals("false");
        if (!forceJar) {
            File localLangFile = new File(plugin.getDataFolder(), language + ".yml");
            if (System.getProperty("nyaautils.i18n.loadFromDisk", "true").equals("true")) {
                if (localLangFile.exists()) {
                    loadLanguageSection(map, YamlConfiguration.loadConfiguration(localLangFile), "", true);
                }
            }
        }
        // load same language from jar
        InputStream stream = plugin.getResource("lang/" + language + ".yml");
        if (stream != null)
            loadLanguageSection(map, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)), "", true);
        // load default language from jar
        stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
        if (stream != null)
            loadLanguageSection(map, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)), "", true);
    }

    /**
     * add all language items from section into language map recursively
     * existing items won't be overwritten
     *
     * @param section        source section
     * @param prefix         used in recursion to determine the proper prefix
     * @param ignoreInternal ignore keys prefixed with `internal'
     */
    private static void loadLanguageSection(Map<String, String> map, ConfigurationSection section, String prefix, boolean ignoreInternal, JavaPlugin plugin) {
        if (map == null || section == null || prefix == null) return;
        for (String key : section.getKeys(false)) {
            String path = prefix + key;
            if (section.isString(key)) {
                if (!map.containsKey(path) && (!ignoreInternal || !path.startsWith("internal."))) {
                    map.put(path, ChatColor.translateAlternateColorCodes('&', section.getString(key)));
                }
            } else if (section.isConfigurationSection(key)) {
                loadLanguageSection(map, section.getConfigurationSection(key), path + ".", ignoreInternal, plugin);
            } else {
                plugin.getLogger().warning("Skipped language section: " + path);
            }
        }
    }

    private void loadLanguageSection(Map<String, String> map, ConfigurationSection section, String prefix, boolean ignoreInternal) {
        loadLanguageSection(map, section, prefix, ignoreInternal, getPlugin());
    }


    /**
     * Load the internal map
     * should only be called from {@link NyaaCoreLoader#onLoad()}
     *
     * @param plugin the NyaaCore plugin
     */
    static void loadInternalMap(NyaaCoreLoader plugin) {
        loadInternalMap(plugin, DEFAULT_LANGUAGE);
    }

    private static void loadInternalMap(NyaaCoreLoader plugin, String language) {
        internalMap.clear();
        boolean forceJar = System.getProperty("nyaautils.i18n.loadFromDisk", "true").equals("false");
        if (!forceJar) { // load from disk
            File localLangFile = new File(plugin.getDataFolder(), language + ".yml");
            if (localLangFile.exists()) {
                ConfigurationSection section = YamlConfiguration.loadConfiguration(localLangFile);
                loadLanguageSection(internalMap, section.getConfigurationSection("internal"), "internal.", false, plugin);
            }
        }
        InputStream stream = plugin.getResource("lang/" + language + ".yml");
        if (stream != null) {
            ConfigurationSection section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            loadLanguageSection(internalMap, section.getConfigurationSection("internal"), "internal.", false, plugin);
        }
        stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
        if (stream != null) {
            ConfigurationSection section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            loadLanguageSection(internalMap, section.getConfigurationSection("internal"), "internal.", false, plugin);
        }
    }

    public String get(@LangKey String key, Object... para) {
        String val = map.get(key);
        if (val == null || val.startsWith("internal.")) val = internalMap.get(key);
        if (val == null) {
            getPlugin().getLogger().warning("Missing language key: " + key);
            StringBuilder keyBuilder = new StringBuilder("MISSING_LANG<" + key + ">");
            for (Object obj : para) {
                keyBuilder.append("#<").append(obj.toString()).append(">");
            }
            return keyBuilder.toString();
        } else {
            try {
                return String.format(val, para);
            } catch (IllegalFormatConversionException e) {
                e.printStackTrace();
                getPlugin().getLogger().warning("Corrupted language key: " + key);
                getPlugin().getLogger().warning("val: " + val);
                StringBuilder keyBuilder = new StringBuilder("");
                for (Object obj : para) {
                    keyBuilder.append("#<").append(obj.toString()).append(">");
                }
                String params = keyBuilder.toString();
                getPlugin().getLogger().warning("params: " + params);
                return "CORRUPTED_LANG<" + key + ">" + params;
            }
        }
    }

    public boolean hasKey(String key) {
        return map.containsKey(key) || internalMap.containsKey(key);
    }
}
