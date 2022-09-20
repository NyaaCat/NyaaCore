package cat.nyaa.nyaacore.utils;

import org.bukkit.ChatColor;

import java.awt.*;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColorUtils {
    //Extracted from EssentialsX
    private static final Pattern REPLACE_ALL_RGB_PATTERN = Pattern.compile("(&)?&#([0-9a-fA-F]{6})");
    private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile("(&)?&([0-9a-fk-orA-FK-OR])");
    private static final Pattern LOGCOLOR_PATTERN = Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]");

    private static String replaceFormat(final String input) {
        if (input == null) {
            return null;
        }
        return replaceColor(input, EnumSet.allOf(ChatColor.class), true);
    }

    //This method is used to simply strip the & convention colour codes
    public static String stripEssentialsFormat(final String input) {
        if (input == null) {
            return null;
        }
        return stripColor(stripColor(input, REPLACE_ALL_PATTERN), REPLACE_ALL_RGB_PATTERN);
    }

    static String stripColor(final String input, final Pattern pattern) {
        return pattern.matcher(input).replaceAll("");
    }

    public static String stripLogColorFormat(final String input) {
        if (input == null) {
            return null;
        }
        return stripColor(input, LOGCOLOR_PATTERN);
    }

    private static String replaceColor(final String input, final Set<ChatColor> supported, boolean rgb) {
        StringBuffer legacyBuilder = new StringBuffer();
        Matcher legacyMatcher = REPLACE_ALL_PATTERN.matcher(input);
        legacyLoop:
        while (legacyMatcher.find()) {
            boolean isEscaped = (legacyMatcher.group(1) != null);
            if (!isEscaped) {
                char code = legacyMatcher.group(2).toLowerCase(Locale.ROOT).charAt(0);
                for (ChatColor color : supported) {
                    if (color.getChar() == code) {
                        legacyMatcher.appendReplacement(legacyBuilder, "\u00a7$2");
                        continue legacyLoop;
                    }
                }
            }
            // Don't change & to section sign (or replace two &'s with one)
            legacyMatcher.appendReplacement(legacyBuilder, "&$2");
        }
        legacyMatcher.appendTail(legacyBuilder);

        if (rgb) {
            StringBuffer rgbBuilder = new StringBuffer();
            Matcher rgbMatcher = REPLACE_ALL_RGB_PATTERN.matcher(legacyBuilder.toString());
            while (rgbMatcher.find()) {
                boolean isEscaped = (rgbMatcher.group(1) != null);
                if (!isEscaped) {
                    try {
                        String hexCode = rgbMatcher.group(2);
                        rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                }
                rgbMatcher.appendReplacement(rgbBuilder, "&#$2");
            }
            rgbMatcher.appendTail(rgbBuilder);
            return rgbBuilder.toString();
        }
        return legacyBuilder.toString();
    }

    /**
     * @throws NumberFormatException If the provided hex color code is invalid or if version is lower than 1.16.
     */
    private static String parseHexColor(String hexColor) throws NumberFormatException {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1); //fuck you im reassigning this.
        }
        if (hexColor.length() != 6) {
            throw new NumberFormatException("Invalid hex length");
        }
        Color.decode("#" + hexColor);
        StringBuilder assembledColorCode = new StringBuilder();
        assembledColorCode.append("\u00a7x");
        for (char curChar : hexColor.toCharArray()) {
            assembledColorCode.append("\u00a7").append(curChar);
        }
        return assembledColorCode.toString();
    }
    //Extracted from EssentialsX    

    //actual call
    public static String hexColored(String str) {
        try {
            return replaceFormat(str);
        } catch (Exception e) {
            //fallback in case an exception thrown.
            return ChatColor.translateAlternateColorCodes('&', str);
        }
    }
}
