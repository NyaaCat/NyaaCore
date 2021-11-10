package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.utils.HexColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * To be extended by each plugin
 * So plugins share the internal language section while maintaining their own sections
 * <p>
 * When NyaaCore loading:
 * Every language available in {@link LanguageRepository#AVAILABLE_INTERNAL_LANGUAGES} is loaded into internalMap
 * And only the internal section is loaded. Then the internalMap remains unchanged unless restart server.
 * When plugin loading:
 * 0. Clear map. So that loading is also reloading
 * 1. None internal sections of DEFAULT_LANGUAGE in NyaaCore language file is loaded into map.
 * 2. None internal sections of given language in NyaaCore language file is loaded into map, overwrite existing keys.
 * 3. Full file of DEFAULT_LANGUAGE in given plugin is loaded into map, overwrite existing keys.
 * 4. Full file of given language in given plugin is loaded into map, overwrite existing keys.
 * 5. Load customized language file from getDataFolder()
 * 6. Dump map to yml file in getDataFolder()
 * When looking for a key:
 * 1. Look up in map
 * 2. Look up in internalMap of given language
 * 3. Look up in internalMap of DEFAULT_LANGUAGE
 * 4. Report a missing key
 */
public abstract class LanguageRepository implements ILocalizer {
    public static Set<String> getAllLanguages() {
        return Arrays.stream(Locale.getAvailableLocales()).map(Locale::toLanguageTag).map(s->s.replace('-', '_')).collect(Collectors.toSet());
    }

    /**
     * Use English as fallback language
     */
    public static final String DEFAULT_LANGUAGE = "en_US";

    /**
     * Per-plugin language map used by {@link LanguageRepository}
     * So it's possible to overwrite some internal language keys here.
     * Map[languageTag, Map[key, value]]
     */
    private final Map<String, Map<String, String>> map = new HashMap<>();

    // helper function to load language map
    private static void loadResourceMap(Plugin plugin, String langTag, Map<String, String> targetMap) {
        if (plugin == null || langTag == null || targetMap == null) throw new IllegalArgumentException();
        InputStream stream = plugin.getResource("lang/" + langTag + ".yml");
        if (stream != null) {
            YamlConfiguration section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            loadLanguageSection(targetMap, section, "");
        }
    }

    // helper function to load language map
    private static void loadLocalMap(Plugin plugin, String codeName, Map<String, String> targetMap) {
        if (plugin == null || codeName == null || targetMap == null) throw new IllegalArgumentException();
        File langFile = new File(plugin.getDataFolder(), codeName + ".yml");
        if (langFile.exists() && langFile.isFile()) {
            YamlConfiguration section = YamlConfiguration.loadConfiguration(langFile);
            loadLanguageSection(targetMap, section, "");
        }
    }

    /**
     * add all language items from section into language map recursively
     * overwrite existing items
     * The '&' will be transformed to color code.
     *
     * @param section        source section
     * @param prefix         used in recursion to determine the proper prefix
     */
    private static void loadLanguageSection(Map<String, String> map, ConfigurationSection section, String prefix) {
        if (map == null || section == null || prefix == null) return;
        for (String key : section.getKeys(false)) {
            String path = prefix + key;
            if (section.isString(key)) {
                map.put(path, HexColorUtils.hexColored(section.getString(key)));
            } else if (section.isConfigurationSection(key)) {
                loadLanguageSection(map, section.getConfigurationSection(key), path + ".");
            }
        }
    }

    /**
     * Dependent plugins should provide the "main" class instance
     * So that language files could be loaded automatically
     *
     * @return the plugin class instance
     */
    protected abstract Plugin getPlugin();

    /**
     * @return the language to be loaded into {@link #map}
     */
    @Deprecated
    protected abstract String getLanguage();

    /**
     * Reset then load per-plugin language map
     * Based on {@link #getPlugin()} and {@link #getLanguage()}
     */
    public void load() {
        this.map.clear();
        for (String langTag : getAllLanguages()) {
            Map<String, String> map = new HashMap<>();
            if (NyaaCoreLoader.getInstance() != null) {
                loadResourceMap(NyaaCoreLoader.getInstance(), langTag, map);
                loadLocalMap(NyaaCoreLoader.getInstance(), langTag, map);
            }
            if (getPlugin() != null) {
                loadResourceMap(getPlugin(), langTag, map);
                loadLocalMap(getPlugin(), langTag, map);
            }
            if (!map.isEmpty()) {
                Bukkit.getConsoleSender().sendMessage(String.format("Loaded language: %s, %d entries.", langTag, map.size()));
                this.map.put(langTag, map);
            }
        }
    }

    /**
     * Get raw value of the language key. Fallback if possible.
     * Return NULL if not found.
     */
    private String getRaw(String key) {
        String val = null;
        if (map.containsKey(getLanguage())) {
            val = map.get(getLanguage()).get(key);
        }
        if (val == null) {
            if (map.containsKey(DEFAULT_LANGUAGE)) {
                val = map.get(DEFAULT_LANGUAGE).get(key);
            }
        }
        return val;
    }

    /**
     * Get the language item then format with `para` by {@link String#format(String, Object...)}
     */
    @Override
    public String getFormatted(String key, Object... para) {
        String val = getRaw(key);
        if (val == null) {
            getPlugin().getLogger().warning("Missing language key: " + key);
            StringBuilder keyBuilder = new StringBuilder("MISSING_LANG<" + key + ">");
            for (Object obj : para) {
                keyBuilder.append("#<").append(obj).append(">");
            }
            return keyBuilder.toString();
        } else {
            try {
                return String.format(val, para);
            } catch (IllegalFormatConversionException e) {
                e.printStackTrace();
                getPlugin().getLogger().warning("Corrupted language key: " + key);
                getPlugin().getLogger().warning("val: " + val);
                StringBuilder keyBuilder = new StringBuilder();
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
    public String getSubstituted(String key, Object... param) {
        if (key == null || param == null || (param.length % 2) != 0) throw new IllegalArgumentException();
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < param.length / 2; i++) {
            if (param[i * 2] != null && param[i * 2 + 1] != null) {
                map.put(param[i * 2], param[i * 2 + 1]);
            }
        }
        return getSubstituted(key, map);
    }

    /**
     * Get the language item specified by `key`
     * Then substitute all `{{paraName}}` with `paraValue`
     * CAVEAT: Replacement occur in arbitrary order.
     *
     * @param key     language item key
     * @param paraMap parameters map, no null key/value allowed
     * @return substituted language item
     */
    public String getSubstituted(String key, Map<?, ?> paraMap) {
        String val = getRaw(key);
        if (val == null) {
            getPlugin().getLogger().warning("Missing language key: " + key);
            StringBuilder keyBuilder = new StringBuilder("MISSING_LANG<" + key + ">");
            for (Map.Entry<?, ?> e : paraMap.entrySet()) {
                keyBuilder.append("#<").append(e.getKey().toString()).append(":").append(e.getValue().toString()).append(">");
            }
            return keyBuilder.toString();
        } else {
            for (Map.Entry<?, ?> e : paraMap.entrySet()) {
                val = val.replace("{{" + e.getKey().toString() + "}}", e.getValue().toString());
            }
            return val;
        }
    }

    @Override
    public boolean hasKey(String key) {
        return getRaw(key) != null;
    }

    public static class InternalOnlyRepository extends LanguageRepository {
        private final String lang;

        public InternalOnlyRepository(String lang) {
            this.lang = lang;
            load();
        }

        public InternalOnlyRepository(Plugin plugin) {
            this.lang = DEFAULT_LANGUAGE;
        }

        @Override
        protected Plugin getPlugin() {
            return null;
        }

        @Override
        protected String getLanguage() {
            return lang;
        }

        @Override
        public String getFormatted(String key, Object... para) {
            if (!key.startsWith("internal.")) throw new IllegalArgumentException("Not an internal language key");
            return super.getFormatted(key, para);
        }

        @Override
        public boolean hasKey(String key) {
            if (!key.startsWith("internal.")) return false;
            return super.hasKey(key);
        }

        @Override
        public String getSubstituted(String key, Map<?, ?> paraMap) {
            if (!key.startsWith("internal.")) throw new IllegalArgumentException("Not an internal language key");
            return super.getSubstituted(key, paraMap);
        }
    }
}
