package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.utils.L10nUtils;
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

    /**
     * Load the internal map
     * should only be called once from {@link NyaaCoreLoader#onLoad()}
     *
     * @param plugin the NyaaCore plugin
     */
    private static NyaaCoreLoader corePlugin = null;
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
            InputStream stream;
            YamlConfiguration section;
            stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
            if (stream != null) {
                section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                loadLanguageSection(map, section, "", false, true);
            }
            stream = plugin.getResource("lang/" + codeName + ".yml");
            if (stream != null) {
                section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                loadLanguageSection(map, section, "", false, true);
            }
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
        InputStream stream;
        YamlConfiguration section;
        stream = corePlugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
        if (stream != null) {
            section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            loadLanguageSection(map, section, "", true, false);
        }
        stream = corePlugin.getResource("lang/" + codeName + ".yml");
        if (stream != null) {
            section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            loadLanguageSection(map, section, "", true, false);
        }

        stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
        if (stream != null) {
            section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            loadLanguageSection(map, section, "", false, false);
        }
        stream = plugin.getResource("lang/" + codeName + ".yml");
        if (stream != null) {
            section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            loadLanguageSection(map, section, "", false, false);
        }

        if ("false".equals(System.getProperty("nyaautils.i18n.refreshLangFiles", "false"))) { // Do not refresh, so still loading from dataFolder
            File localLangFile = new File(plugin.getDataFolder(), codeName + ".yml");
            if (localLangFile.exists() && localLangFile.isFile()) {
                loadLanguageSection(map, YamlConfiguration.loadConfiguration(localLangFile), "", false, false);
            }
        }

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

        plugin.getLogger().info(get("internal.info.using_language", codeName));
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

    public String get(@LangKey String key, Object... para) {
        String val = map.get(key);
        if (val == null && key.startsWith("internal.") && internalMap.containsKey(getLanguage())) {
            internalMap.get(getLanguage()).get(key);
        }
        if (val == null && key.startsWith("internal.")) {
            internalMap.get(DEFAULT_LANGUAGE).get(key);
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

    public boolean hasKey(String key) {
        if (map.containsKey(key) || internalMap.get(DEFAULT_LANGUAGE).containsKey(key)) return true;
        if (internalMap.containsKey(getLanguage()) && internalMap.get(getLanguage()).containsKey(key)) return true;
        return false;
    }
}
