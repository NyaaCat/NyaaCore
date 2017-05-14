/*
 * Copyright (c) 2015 Jerrell Fang
 *
 * This project is Open Source and distributed under The MIT License (MIT)
 * (http://opensource.org/licenses/MIT)
 *
 * You should have received a copy of the The MIT License along with
 * this project.   If not, see <http://opensource.org/licenses/MIT>.
 */

package cat.nyaa.nyaacore.utils;

import cat.nyaa.nyaacore.NyaaCoreLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Meow J on 6/20/2015.
 * <p>
 * Unlocalized Name to Localized Name
 *
 * @author Meow J
 */
public final class L10nUtils {
    public enum AvailableLanguages {
        ENGLISH("en_US"),
        CHINESE_SIMPLIFIED("zh_CN");
        public final String codeName;
        public final Map<String, String> map;
        AvailableLanguages(String codeName) {
            this.codeName = codeName;
            this.map = new HashMap<>();
        }
    };


    private static final Map<String, AvailableLanguages> lookup = new HashMap<>();

    static {
        for (AvailableLanguages lang : EnumSet.allOf(AvailableLanguages.class))
            lookup.put(lang.codeName.toLowerCase(), lang);
    }

    /**
     * @param locale The locale of the language
     * @return The index of a lang file based on locale.
     */
    public static AvailableLanguages get(String locale) {
        AvailableLanguages result = lookup.get(locale);
        return result == null ? AvailableLanguages.ENGLISH : result;
    }

    /**
     * Initialize this class, load all the languages to the corresponding HashMap.
     */
    public static void init(NyaaCoreLoader plugin) {
        for (AvailableLanguages i16rLang : AvailableLanguages.values()) {
            try {
                readFile(i16rLang, new BufferedReader(new InputStreamReader(plugin.getResource("l10n/" + i16rLang.codeName + ".lang"), Charset.forName("UTF-8"))));
                plugin.getLogger().info(i16rLang.codeName + " has been loaded.");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load language file " + i16rLang.codeName);
                e.printStackTrace();
            }
        }
    }

    public static void readFile(AvailableLanguages i16rLang, BufferedReader reader) throws IOException {
        String temp;
        String[] tempStringArr;
        try {
            temp = reader.readLine();
            while (temp != null) {
                if (temp.startsWith("#")) continue;
                if (temp.contains("=")) {
                    tempStringArr = temp.split("=");
                    i16rLang.map.put(tempStringArr[0], tempStringArr.length > 1 ? tempStringArr[1] : "");
                }
                temp = reader.readLine();
            }
        } finally {
            reader.close();
        }
    }
}
