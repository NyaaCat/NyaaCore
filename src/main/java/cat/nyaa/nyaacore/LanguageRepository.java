package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.utils.L10nUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.librazy.nyaautils_lang_checker.LangKey;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Map;
import java.util.TreeMap;

/**
 * To be extended by each plugin
 * So plugins share the internal language section while maintaining their own sections
 *
 * When NyaaCore loading:
 *   Every language available in {@link cat.nyaa.nyaacore.utils.L10nUtils.AvailableLanguages} is loaded into internalMap
 *   And only the internal section is loaded. Then the internalMap remains unchanged unless restart server.
 * When plugin loading:
 *   0. Clear map. So that loading is also reloading
 *   1. None internal sections of DEFAULT_LANGUAGE in NyaaCore language file is loaded into map.
 *   2. None internal sections of given language in NyaaCore language file is loaded into map, overwrite existing keys.
 *   3. Full file of DEFAULT_LANGUAGE in given plugin is loaded into map, overwrite existing keys.
 *   4. Full file of given language in given plugin is loaded into map, overwrite existing keys.
 *   5. Load customized language file from getDataFolder()
 *   6. Dump map to yml file in getDataFolder()
 * When looking for a key:
 *   1. Look up in map
 *   2. Look up in internalMap of given language
 *   3. Look up in internalMap of DEFAULT_LANGUAGE
 *   4. Report a missing key
 *
 * Use -Dnyaautils.i18n.refreshLangFiles=true to force loading languages from jar.
 */
public abstract class LanguageRepository {
    /**
     * Use English as default & fallback language
     */
    private static final String DEFAULT_LANGUAGE = L10nUtils.AvailableLanguages.ENGLISH.codeName;
    /**
     * Internal language map is loaded and should only be loaded by {@link NyaaCoreLoader#onLoad()}
     * Once it's loaded, it cannot be reloaded or modified.
     * The internal map will be shared across all plugins using {@link LanguageRepository}
     */
    private final static Map<String, Map<String, String>> internalMap = new HashMap<>();
    /**
     * Per-plugin language map used by {@link LanguageRepository}
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

    private static NyaaCoreLoader corePlugin = null;

    // helper function to load language map
    private static void loadResourceMap(JavaPlugin plugin, String codeName,
                                        Map<String, String> targetMap, boolean ignoreInternal, boolean ignoreNormal) {
        if (plugin == null || codeName == null || targetMap == null) throw new IllegalArgumentException();
        InputStream stream = plugin.getResource("lang/" + codeName + ".yml");
        if (stream != null) {
            YamlConfiguration section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            loadLanguageSection(targetMap, section, "", ignoreInternal, ignoreNormal);
        }
    }

    // helper function to load language map
    private static void loadLocalMap(JavaPlugin plugin, String codeName,
                                        Map<String, String> targetMap, boolean ignoreInternal, boolean ignoreNormal) {
        if (plugin == null || codeName == null || targetMap == null) throw new IllegalArgumentException();
        if (Boolean.parseBoolean(System.getProperty("nyaautils.i18n.refreshLangFiles", "false"))) return;
        File langFile = new File(plugin.getDataFolder(), codeName + ".yml");
        if (langFile.exists() && langFile.isFile()) {
            YamlConfiguration section = YamlConfiguration.loadConfiguration(langFile);
            loadLanguageSection(targetMap, section, "", ignoreInternal, ignoreNormal);
        }
    }

    /**
     * Load the internal map
     * should only be called once from {@link NyaaCoreLoader#onLoad()}
     *
     * @param plugin the NyaaCore plugin
     */
    public static void initInternalMap(NyaaCoreLoader plugin) {
        if (internalMap.size() != 0 || corePlugin != null) {
            plugin.getLogger().warning("Multiple internalMap initiation");
            return;
        }
        corePlugin = plugin;
        for (L10nUtils.AvailableLanguages lang : L10nUtils.AvailableLanguages.values()) {
            String codeName = lang.codeName;
            Map<String, String> map = new HashMap<>();
            internalMap.put(codeName, map);
            loadResourceMap(plugin, DEFAULT_LANGUAGE, map, false, true);
            loadLocalMap(plugin, DEFAULT_LANGUAGE, map, false, true);
            loadResourceMap(plugin, codeName, map, false, true);
            loadLocalMap(plugin, codeName, map, false, true);
            plugin.getLogger().info(String.format("NyaaCore internalMap loaded: %s", codeName));
        }
    }

    /**
     * Reset then load per-plugin language map
     * Based on {@link this#getPlugin()} and {@link this#getLanguage()}
     */
    public void load() {
        String codeName = getLanguage();
        JavaPlugin plugin = getPlugin();
        if (codeName == null) codeName = DEFAULT_LANGUAGE;
        map.clear();
        // load languages
        loadResourceMap(corePlugin, DEFAULT_LANGUAGE, map, true, false);
        loadLocalMap(corePlugin, DEFAULT_LANGUAGE, map, true, false);
        loadResourceMap(corePlugin, codeName, map, true, false);
        loadLocalMap(corePlugin, codeName, map, true, false);

        loadResourceMap(getPlugin(), DEFAULT_LANGUAGE, map, false, false);
        loadLocalMap(getPlugin(), DEFAULT_LANGUAGE, map, false, false);
        loadResourceMap(getPlugin(), codeName, map, false, false);
        loadLocalMap(getPlugin(), codeName, map, false, false);

        // save (probably) modified language file back to disk
        File localLangFile = new File(plugin.getDataFolder(), codeName + ".yml");
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            for (String key : map.keySet()) {
                yaml.set(key, map.get(key));
            }
            yaml.save(localLangFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Cannot save language file: " + codeName + ".yml");
        }

        plugin.getLogger().info(getFormatted("internal.info.using_language", codeName));
    }

    /**
     * add all language items from section into language map recursively
     * overwrite existing items
     * The '&' will be transformed to color code.
     *
     * @param section        source section
     * @param prefix         used in recursion to determine the proper prefix
     * @param ignoreInternal ignore keys prefixed with `internal'
     * @param ignoreNormal   ignore keys not prefixed with `internal'
     */
    private static void loadLanguageSection(Map<String, String> map, ConfigurationSection section, String prefix, boolean ignoreInternal, boolean ignoreNormal) {
        if (map == null || section == null || prefix == null) return;
        for (String key : section.getKeys(false)) {
            String path = prefix + key;
            if (section.isString(key)) {
                if (path.startsWith("internal") && ignoreInternal) continue;
                if (!path.startsWith("internal") && ignoreNormal) continue;
                map.put(path, ChatColor.translateAlternateColorCodes('&', section.getString(key)));
            } else if (section.isConfigurationSection(key)) {
                loadLanguageSection(map, section.getConfigurationSection(key), path + ".", ignoreInternal, ignoreNormal);
            }
        }
    }

    /**
     * @deprecated rename to getFormatted
     */
    public String get(@LangKey String key, Object... para) {
        return getFormatted(key, para);
    }

    /**
     * Get the language item then format with `para` by {@link String#format(String, Object...)}
     */
    public String getFormatted(@LangKey String key, Object... para) {
        String val = map.get(key);
        if (val == null && key.startsWith("internal.") && internalMap.containsKey(getLanguage())) {
            val = internalMap.get(getLanguage()).get(key);
        }
        if (val == null && key.startsWith("internal.")) {
            val = internalMap.get(DEFAULT_LANGUAGE).get(key);
        }
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

    /**
     * Called this way: `getSubstituted(languageKey, "key1", value1, "key2", value2, ...)`
     * {@link Object#toString()} will be called on non-string objects
     * `null` key-value pair will be ignored
     */
    public String getSubstituted(@LangKey String key, Object... param) {
        if (key == null || param == null || (param.length%2) != 0) throw new IllegalArgumentException();
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0;i<param.length/2;i++) {
            if (param[i*2] != null && param[i*2+1]!=null) {
                map.put(param[i*2], param[i*2+1]);
            }
        }
        return getSubstituted(key, map);
    }

    /**
     * Get the language item specified by `key`
     * Then substitute all `{{paraName}}` with `paraValue`
     * CAVEAT: Replacement occur in arbitrary order.
     * @param key language item key
     * @param paraMap parameters map, no null key/value allowed
     * @return substituted language item
     */
    public String getSubstituted(@LangKey String key, Map<?,?> paraMap) {
        String val = map.get(key);
        if (val == null && key.startsWith("internal.") && internalMap.containsKey(getLanguage())) {
            val = internalMap.get(getLanguage()).get(key);
        }
        if (val == null && key.startsWith("internal.")) {
            val = internalMap.get(DEFAULT_LANGUAGE).get(key);
        }
        if (val == null) {
            getPlugin().getLogger().warning("Missing language key: " + key);
            StringBuilder keyBuilder = new StringBuilder("MISSING_LANG<" + key + ">");
            for (Map.Entry<?,?> e : paraMap.entrySet()) {
                keyBuilder.append("#<").append(e.getKey().toString()).append(":").append(e.getValue().toString()).append(">");
            }
            return keyBuilder.toString();
        } else {
            for (Map.Entry<?,?> e : paraMap.entrySet()) {
                val = val.replace("{{" + e.getKey().toString() + "}}", e.getValue().toString());
            }
            return val;
        }
    }

    public boolean hasKey(String key) {
        if (map.containsKey(key) || internalMap.get(DEFAULT_LANGUAGE).containsKey(key)) return true;
        if (internalMap.containsKey(getLanguage()) && internalMap.get(getLanguage()).containsKey(key)) return true;
        return false;
    }
}
