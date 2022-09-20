package cat.nyaa.nyaacore.cmdreceiver;

import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Arguments {
    private List<String> parsedArguments = new ArrayList<>();
    private int index = 0;
    private CommandSender sender;
    private String[] rawArgs;
    private Arguments() {
    }

    private Arguments(CommandSender sender) {
        this.sender = sender;
    }

    public static Arguments parse(String[] rawArg) {
        return parse(rawArg, null);
    }

    public static Arguments parsePreserveLastBlank(String[] rawArg, CommandSender sender) {
        if (rawArg[rawArg.length - 1].isEmpty()) {
            Arguments arg = parse(rawArg, sender);
            arg.parsedArguments.add("");
            arg.rawArgs = rawArg;
            return arg;
        } else {
            return parse(rawArg, sender);
        }
    }

    public static Arguments parse(String[] rawArg, CommandSender sender) {
        try {
            return parseChecked(rawArg, sender);
        } catch (ArgumentParsingException ex) {
            return null;
        }
    }

    // Arguments are seperated by 1 or more whitespaces.
    // Arguments containing whitespaces can be surrounded in backticks or double quotes.
    // Quotes in quotes can be escaped using backslash.
    // Cannot escape backticks in double quotes, and vice versa.
    // Escape backslash using backslash.
    // Quotation must cover the whole argument.
    // As an exception, key:`val` will be interpreted as `key:val`
    public static Arguments parseChecked(String[] rawArg, CommandSender sender) throws ArgumentParsingException {
        if (rawArg.length == 0) return new Arguments(sender);
        String cmd = rawArg[0];
        for (int i = 1; i < rawArg.length; i++)
            cmd += " " + rawArg[i];

        List<String> cmdList = new ArrayList<>();
        String tmp = "";
        ParserState state = ParserState.EXPECT_ARG;
        for (int i = 0; i <= cmd.length(); i++) {
            Character chr = i < cmd.length() ? cmd.charAt(i) : null;
            Character next_char = i + 1 < cmd.length() ? cmd.charAt(i + 1) : null;

            switch (state) {
                case EXPECT_ARG -> {
                    if (chr == null || chr == ' ') {

                    } else if (chr == '"') {
                        state = ParserState.IN_QUOTE;
                    } else if (chr == '`') {
                        state = ParserState.IN_BACKTICK;
                    } else if (chr == ':') {
                        tmp += chr;
                        state = ParserState.IN_UNQUOTE_AFTER_COLON;
                    } else {
                        tmp += chr;
                        state = ParserState.IN_UNQUOTE_BEFORE_COLON;
                    }
                }
                case IN_UNQUOTE_BEFORE_COLON -> {
                    if (chr == null || chr == ' ') {
                        cmdList.add(tmp);
                        tmp = "";
                        state = ParserState.EXPECT_ARG;
                    } else if (chr == '\\' || chr == '"' || chr == '`') {
                        throw new ArgumentParsingException(cmd, i, "cannot escape/quote here");
                    } else if (chr == ':') {
                        tmp += chr;
                        if (next_char == null) {
                            state = ParserState.IN_UNQUOTE_AFTER_COLON;
                        } else if (next_char == '"') {
                            state = ParserState.IN_QUOTE;
                            i++;
                        } else if (next_char == '`') {
                            state = ParserState.IN_BACKTICK;
                            i++;
                        } else {
                            state = ParserState.IN_UNQUOTE_AFTER_COLON;
                        }
                    } else {
                        tmp += chr;
                    }
                }
                case IN_UNQUOTE_AFTER_COLON -> {
                    if (chr == null || chr == ' ') {
                        cmdList.add(tmp);
                        tmp = "";
                        state = ParserState.EXPECT_ARG;
                    } else if (chr == '\\' || chr == '"' || chr == '`') {
                        throw new ArgumentParsingException(cmd, i, "cannot escape/quote here");
                    } else {
                        tmp += chr;
                    }
                }
                case IN_BACKTICK -> {
                    if (chr == null) {
                        throw new ArgumentParsingException(cmd, i, "Unfinished backtick");
                    } else if (chr == '\\') {
                        state = ParserState.IN_BACKTICK_ESCAPE;
                    } else if (chr == '`') {
                        if (next_char != null && next_char != ' ') {
                            throw new ArgumentParsingException(cmd, i, "Backtick cannot stop here");
                        }
                        cmdList.add(tmp);
                        tmp = "";
                        state = ParserState.EXPECT_ARG;
                    } else {
                        tmp += chr;
                    }
                }
                case IN_BACKTICK_ESCAPE -> {
                    if (chr == null) {
                        throw new ArgumentParsingException(cmd, i, "Unfinished escape");
                    } else if (chr == '\\' || chr == '`') {
                        tmp += chr;
                        state = ParserState.IN_BACKTICK;
                    } else {
                        throw new ArgumentParsingException(cmd, i, "Invalid escape char in backtick block");
                    }
                }
                case IN_QUOTE -> {
                    if (chr == null) {
                        throw new ArgumentParsingException(cmd, i, "Unfinished double quote");
                    } else if (chr == '\\') {
                        state = ParserState.IN_QUOTE_ESCAPE;
                    } else if (chr == '"') {
                        if (next_char != null && next_char != ' ') {
                            throw new ArgumentParsingException(cmd, i, "Double quote cannot stop here");
                        }
                        cmdList.add(tmp);
                        tmp = "";
                        state = ParserState.EXPECT_ARG;
                    } else {
                        tmp += chr;
                    }
                }
                case IN_QUOTE_ESCAPE -> {
                    if (chr == null) {
                        throw new ArgumentParsingException(cmd, i, "Unfinished escape");
                    } else if (chr == '\\' || chr == '"') {
                        tmp += chr;
                        state = ParserState.IN_QUOTE;
                    } else {
                        throw new ArgumentParsingException(cmd, i, "Invalid escape char in double quote block");
                    }
                }
            }
        }

        Arguments ret = new Arguments(sender);
        ret.parsedArguments = cmdList;
        ret.rawArgs = rawArg;
        return ret;
    }

    public static <T extends Enum<T>> T parseEnum(Class<T> cls, String str) {
        try {
            return Enum.valueOf(cls, str);
        } catch (IllegalArgumentException ex) {
            String vals = "";
            List<String> l = new ArrayList<>();
            for (T k : cls.getEnumConstants()) {
                l.add(k.name());
            }
            l.sort(Comparator.naturalOrder());
            for (String k : l) vals += "\n" + k;

            throw new BadCommandException("internal.error.bad_enum", cls.getName(), vals);
        }
    }

    /**
     * @deprecated hard coded indexes are not recommended
     */
    @Deprecated
    public String at(int index) {
        return parsedArguments.get(index);
    }

    /**
     * how many times you can call {@link Arguments#next()} before you get an null.
     */
    public int remains() {
        return parsedArguments.size() - index;
    }

    public boolean isSuggestion() {
        return parsedArguments.get(parsedArguments.size() - 1).isEmpty();
    }

    public String[] getRawArgs() {
        return rawArgs;
    }

    public String next() {
        if (index < parsedArguments.size())
            return parsedArguments.get(index++);
        else
            return null;
    }

    public String top() {
        if (index < parsedArguments.size())
            return parsedArguments.get(index);
        else
            return null;
    }

    public int nextInt() {
        String str = next();
        if (str == null) throw new BadCommandException("internal.error.no_more_int");
        if (str.endsWith("k")) str = str.substring(0, str.length() - 1) + "000";
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new BadCommandException("internal.error.bad_int", ex, str);
        }
    }

    public Integer nextInt(Integer defaultVal) {
        int curIndex = index;
        try {
            return nextInt();
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public long nextLong() {
        String str = next();
        if (str == null) throw new BadCommandException("internal.error.no_more_int");
        if (str.endsWith("k")) str = str.substring(0, str.length() - 1) + "000";
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            throw new BadCommandException("internal.error.bad_int", ex, str);
        }
    }

    public Long nextLong(Long defaultVal) {
        int curIndex = index;
        try {
            return nextLong();
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public double nextDouble() {
        String str = next();
        if (str == null) throw new BadCommandException("internal.error.no_more_double");
        try {
            double d = Double.parseDouble(str);
            if (Double.isInfinite(d) || Double.isNaN(d))
                throw new BadCommandException("internal.error.no_more_double");
            return d;
        } catch (NumberFormatException ex) {
            throw new BadCommandException("internal.error.bad_double", ex, str);
        }
    }

    public Double nextDouble(Double defaultVal) {
        int curIndex = index;
        try {
            return nextDouble();
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    // throw exception if no string found
    public String nextString() {
        String str = next();
        if (str == null) throw new BadCommandException("internal.error.no_more_string");
        return str;
    }

    public String nextString(String defaultVal) {
        int curIndex = index;
        try {
            return nextString();
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public double nextDouble(String pattern) {
        String str = next();
        if (str == null) throw new BadCommandException("internal.error.no_more_double");
        try {
            double d = Double.parseDouble(str);
            if (Double.isInfinite(d) || Double.isNaN(d))
                throw new BadCommandException("internal.error.no_more_double");
            return Double.parseDouble(new DecimalFormat(pattern).format(d));
        } catch (NumberFormatException ex) {
            throw new BadCommandException("internal.error.bad_double", ex, str);
        } catch (IllegalArgumentException ex) {
            throw new BadCommandException("internal.error.bad_decimal_pattern", ex, pattern);
        }
    }

    public Double nextDouble(String pattern, Double defaultVal) {
        int curIndex = index;
        try {
            return nextDouble(pattern);
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public <T extends Enum<T>> T nextEnum(Class<T> cls) {
        String str = next();
        if (str == null) throw new BadCommandException("internal.error.no_more_enum");
        return parseEnum(cls, str);
    }

    public <T extends Enum<T>> T nextEnum(Class<T> cls, T defaultVal) {
        int curIndex = index;
        try {
            return nextEnum(cls);
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public boolean nextBoolean() {
        String str = next();
        if (str == null) throw new BadCommandException("internal.error.no_more_bool");
        return Boolean.parseBoolean(str);
    }

    public Boolean nextBoolean(Boolean defaultVal) {
        int curIndex = index;
        try {
            return nextBoolean();
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public Player nextPlayer() {
        String name = next();
        if (name == null) throw new BadCommandException("internal.error.no_more_player");
        if (name.startsWith("@")) {
            List<Entity> entities = Bukkit.selectEntities(sender, name);
            if (entities.size() != 1) {
                throw new BadCommandException("internal.error.no_more_player"); // TODO: more descriptive msg
            }
            Entity entity = entities.get(0);
            if (!(entity instanceof Player)) {
                throw new BadCommandException("internal.error.no_more_player"); // TODO: more descriptive msg
            }
            return (Player) entity;
        }
        try {
            UUID uuid = UUID.fromString(name);
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) throw new BadCommandException("internal.error.player_not_found", name);
            return p;
        } catch (IllegalArgumentException ignored) {
        }
        Player p = Bukkit.getPlayer(name);
        if (p == null) throw new BadCommandException("internal.error.player_not_found", name);
        return p;
    }

    public Player nextPlayer(Player defaultVal) {
        int curIndex = index;
        try {
            return nextPlayer();
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public Player nextPlayerByName() {
        String name = next();
        if (name == null) throw new BadCommandException("internal.error.no_more_player");
        Player p = Bukkit.getPlayer(name);
        if (p == null) throw new BadCommandException("internal.error.player_not_found", name);
        return p;
    }

    public Player nextPlayerOrSender() {
        if (top() == null && sender instanceof Player) {
            return (Player) sender;
        }
        return nextPlayer();
    }

    public Entity nextEntity() {
        String name = next();
        if (name == null) throw new BadCommandException("internal.error.no_more_entity");
        if (name.startsWith("@")) {
            List<Entity> entities = Bukkit.selectEntities(sender, name);
            if (entities.size() != 1) {
                throw new BadCommandException("internal.error.no_more_entity"); // TODO: more descriptive msg
            }
            return entities.get(0);
        }
        try {
            UUID uuid = UUID.fromString(name);
            Entity p = Bukkit.getEntity(uuid);
            if (p == null) throw new BadCommandException("internal.error.entity_not_found", name);
            return p;
        } catch (IllegalArgumentException ignored) {
        }
        Player p = Bukkit.getPlayer(name);
        if (p == null) throw new BadCommandException("internal.error.entity_not_found", name);
        return p;
    }

    public Entity nextEntity(Entity defaultVal) {
        int curIndex = index;
        try {
            return nextEntity();
        } catch (Throwable e) {
            index = curIndex;
            return defaultVal;
        }
    }

    public Entity nextEntityOrSender() {
        if (top() == null && sender instanceof Player) {
            return (Player) sender;
        }
        return nextEntity();
    }

    public OfflinePlayer nextOfflinePlayer() {
        String name = next();
        if (Strings.isNullOrEmpty(name)) throw new BadCommandException("internal.error.no_more_player");
        OfflinePlayer player = OfflinePlayerUtils.lookupPlayer(name);
        if (player == null) throw new BadCommandException("internal.error.player_not_found", name);
        return player;
    }

    public List<Entity> nextSelectorEntities() {
        String name = next();
        return Bukkit.selectEntities(sender, name);
    }

    public Arguments nextAssert(String string) {
        String top = next();
        if (top == null && string == null) return this;
        if (top == null || string == null) throw new BadCommandException("internal.error.assert_fail", string, top);
        if (!string.equals(top)) throw new BadCommandException("internal.error.assert_fail", string, top);
        return this;
    }

    /**
     * fetch an named argument from remaining list
     * if multiple arguments, the first one will be returned
     * the returned argument will be removed from further consideration
     * named arguments looks like this: `argument_name:argument_value`
     *
     * @param key argument name
     * @return the value as string
     */
    public String argString(String key) {
        int j = index;
        while (j < length() && !parsedArguments.get(j).startsWith(key + ":")) {
            j++;
        }
        if (j >= length()) throw new BadCommandException("internal.named_argument.missing_arg", key);
        String fullArgument = parsedArguments.get(j);
        String value = fullArgument.substring(key.length() + 1);

        // rearrange order
        parsedArguments.remove(j);
        parsedArguments.add(index, fullArgument);
        index++;
        return value;
    }

    // Note: all `arg*()` functions will rearrange argument order
    // and mess up indexes. Thus, it's not recommended to use together with `at()` function

    /**
     * get named argument as string
     *
     * @param key argument name
     * @param def default value
     * @return argument value
     */
    public String argString(String key, String def) {
        try {
            return argString(key);
        } catch (BadCommandException ex) {
            return def;
        }
    }

    /**
     * get named argument as integer
     *
     * @param key argument name
     * @return int value
     */
    public int argInt(String key) {
        String str = argString(key);
        if (str.endsWith("k")) str = str.substring(0, str.length() - 1) + "000";
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new BadCommandException("internal.named_argument.not_int", ex, key, str);
        }
    }

    /**
     * get named argument as integer
     *
     * @param key argument name
     * @param def default value
     * @return int value
     */
    public int argInt(String key, int def) {
        try {
            return argInt(key);
        } catch (BadCommandException ex) {
            return def;
        }
    }

    public int length() {
        return parsedArguments.size();
    }

    private enum ParserState {
        EXPECT_ARG,
        IN_UNQUOTE_BEFORE_COLON,
        IN_UNQUOTE_AFTER_COLON,
        IN_BACKTICK,
        IN_BACKTICK_ESCAPE,
        IN_QUOTE,
        IN_QUOTE_ESCAPE
    }
}
