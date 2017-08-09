package cat.nyaa.nyaacore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.librazy.nyaautils_lang_checker.LangKey;
import org.librazy.nyaautils_lang_checker.LangKeyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

@SuppressWarnings("unused")
public abstract class CommandReceiver implements CommandExecutor, TabCompleter {
    //==== Error Definitions ====//
    private static class NotPlayerException extends RuntimeException {
    }

    private static class NoItemInHandException extends RuntimeException {
        public final boolean isOffHand;

        /**
         * @param ifh true if require item in offhand
         */
        public NoItemInHandException(boolean ifh) {
            isOffHand = ifh;
        }
    }

    protected static class NoPermissionException extends RuntimeException {
        /**
         * @param permission name for the permission node
         */
        public NoPermissionException(String permission) {
            super(permission);
        }
    }

    protected static class BadCommandException extends RuntimeException {
        public final Object[] objs;

        public BadCommandException() {
            super("");
            objs = null;
        }

        /**
         * show formatted error message to player
         *
         * @param msg_internal msg template key. e.g. `internal.warn.***'
         * @param args         arguments
         */
        public BadCommandException(@LangKey String msg_internal, Object... args) {
            super(msg_internal);
            objs = args;
        }

        public BadCommandException(@LangKey(varArgsPosition = 1) String msg_internal, Throwable cause, Object... args) {
            super(msg_internal, cause);
            objs = args;
        }
    }

    // Language class is passed in for message support
    private final ILocalizer i18n;
    // Subcommands exists in this class
    protected final Map<String, Method> subCommands = new HashMap<>();
    // Commands to be passed to other classes
    protected final Map<String, CommandReceiver> subCommandClasses = new HashMap<>();
    // Permissions required for each subclass. Bypass check if no permission specified
    protected final Map<String, String> subCommandPermission = new HashMap<>();
    // Custom attributes
    protected final Map<String, String> subCommandAttribute = new HashMap<>();

    private Set<Method> getAllMethods(Class cls) {
        Set<Method> ret = new HashSet<>();
        while (cls != null) {
            ret.addAll(Arrays.asList(cls.getDeclaredMethods()));
            cls = cls.getSuperclass();
        }
        return ret;
    }

    private Set<Field> getAllFields(Class cls) {
        Set<Field> ret = new HashSet<>();
        while (cls != null) {
            ret.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return ret;
    }

    // Scan recursively into parent class to find annotated methods when constructing
    public CommandReceiver(JavaPlugin plugin, ILocalizer _i18n) {
        if (plugin == null) throw new IllegalArgumentException();
        if (_i18n == null) _i18n = new LanguageRepository.InternalOnlyRepository(plugin);

        this.i18n = _i18n;

        for (Method m : getAllMethods(getClass())) {
            SubCommand anno = m.getAnnotation(SubCommand.class);
            if (anno == null) continue;
            Class<?>[] params = m.getParameterTypes();
            if (!(params.length == 2 &&
                    params[0] == CommandSender.class &&
                    params[1] == Arguments.class)) {
                plugin.getLogger().warning(i18n.getFormatted("internal.error.bad_subcommand", m.toString()));
            } else {
                m.setAccessible(true);
                subCommands.put(anno.value().toLowerCase(), m);
                if (!anno.permission().equals(""))
                    subCommandPermission.put(anno.value(), anno.permission());
                if (!anno.attribute().equals(""))
                    subCommandAttribute.put(anno.value(), anno.attribute());
            }
        }

        for (Field f : getAllFields(getClass())) {
            SubCommand anno = f.getAnnotation(SubCommand.class);
            if (anno == null) continue;
            if (CommandReceiver.class.isAssignableFrom(f.getType())) {
                CommandReceiver obj = null;
                try {
                    obj = newInstance(f.getType(),plugin, i18n);
                    if (obj != null) {
                        subCommandClasses.put(anno.value().toLowerCase(), obj);
                        f.setAccessible(true);
                        f.set(this, obj);
                    }
                } catch (ReflectiveOperationException ex) {
                    plugin.getLogger().warning(i18n.getFormatted("internal.error.bad_subcommand", f.toString()));
                    obj = null;
                    ex.printStackTrace();
                }
            } else {
                plugin.getLogger().warning(i18n.getFormatted("internal.error.bad_subcommand", f.toString()));
            }
        }
    }

    private CommandReceiver newInstance(Class cls, Object arg1, Object arg2) throws ReflectiveOperationException {
        for (Constructor c : cls.getConstructors()) {
            if (c.getParameterCount() == 2 &&
                    c.getParameterTypes()[0].isAssignableFrom(arg1.getClass()) &&
                    c.getParameterTypes()[1].isAssignableFrom(arg2.getClass())) {
                return (CommandReceiver) c.newInstance(arg1, arg2);
            }
        }
        throw new NoSuchMethodException("no matching constructor found");
    }

    public List<String> getSubcommands() {
        ArrayList<String> ret = new ArrayList<>();
        ret.addAll(subCommands.keySet());
        ret.addAll(subCommandClasses.keySet());
        ret.sort(String::compareTo);
        return ret;
    }

    // Only directly registered command handler need this
    // acceptCommand() will be called directly in subcommand classes
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Arguments cmd = Arguments.parse(args);
        if (cmd == null) return false;
        acceptCommand(sender, cmd);
        return true;
    }

    // Determine subcommand method or class and Exception collection.
    // Can be overrided for finer subcommand routing
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        String subCommand = cmd.next();
        try {
            if (subCommand == null) subCommand = "help";
            boolean hasCommand = subCommands.containsKey(subCommand.toLowerCase()) || subCommandClasses.containsKey(subCommand.toLowerCase());
            boolean subClassCommand = subCommandClasses.containsKey(subCommand.toLowerCase());
            if (cmd.length() == 0 || !hasCommand) {
                subCommand = "help";
            }

            if (subCommandPermission.containsKey(subCommand)) {
                if (!sender.hasPermission(subCommandPermission.get(subCommand))) {
                    throw new NoPermissionException(subCommandPermission.get(subCommand));
                }
            }

            try {
                if (subClassCommand) {
                    subCommandClasses.get(subCommand.toLowerCase()).acceptCommand(sender, cmd);
                } else {
                    subCommands.get(subCommand.toLowerCase()).invoke(this, sender, cmd);
                }
            } catch (ReflectiveOperationException ex) {
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                else
                    throw new RuntimeException("Failed to invoke subcommand", ex);
            }
            if (!subClassCommand) msg(sender, "internal.info.command_complete");
        } catch (NotPlayerException ex) {
            msg(sender, "internal.error.not_player");
        } catch (NoItemInHandException ex) {
            msg(sender, ex.isOffHand ? "internal.error.no_item_offhand" : "internal.error.no_item_hand");
        } catch (BadCommandException ex) {
            @LangKey(skipCheck = true) String msg = ex.getMessage();
            if (msg != null && !msg.equals("")) {
                if (ex.objs == null) {
                    msg(sender, msg);
                } else {
                    msg(sender, msg, ex.objs);
                }
            } else {
                msg(sender, "internal.error.invalid_command_arg");
            }
            msg(sender, "internal.info.usage_prompt",
                    getHelpContent("usage", getHelpPrefix(), subCommand));
        } catch (NoPermissionException ex) {
            msg(sender, "internal.error.no_required_permission", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            msg(sender, "internal.error.command_exception");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            Arguments cmd = Arguments.parse(args);
            if (cmd == null) return null;
            return acceptTabComplete(sender, cmd);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<String> acceptTabComplete(CommandSender sender, Arguments args) throws Exception {
        String cmd = args.next();
        if (cmd == null) cmd = "";
        if (subCommands.containsKey(cmd.toLowerCase())) return null;
        if (subCommandClasses.containsKey(cmd.toLowerCase()))
            return subCommandClasses.get(cmd.toLowerCase()).acceptTabComplete(sender, args);
        List<String> arr = new ArrayList<>();
        for (String s : getSubcommands()) {
            if (subCommandPermission.containsKey(s)) {
                if (!sender.hasPermission(subCommandPermission.get(s))) {
                    continue;
                }
            }
            if (s.toLowerCase().startsWith(cmd.toLowerCase())) {
                arr.add(s);
            }
        }
        return arr;
    }

    public abstract String getHelpPrefix();

    private String getHelpContent(@LangKey(type = LangKeyType.SUFFIX, skipCheck = true) String type, String... subkeys) {
        @LangKey(type = LangKeyType.PREFIX, skipCheck = true) String key = "manual";
        for (String s : subkeys) {
            if (s.length() > 0)
                key += "." + s;
        }
        key += "." + type;
        if (i18n.hasKey(key)) {
            return i18n.getFormatted(key);
        } else {
            return i18n.getFormatted("manual.no_" + type);
        }
    }

    @SubCommand("help")
    public void printHelp(CommandSender sender, Arguments args) {
        List<String> cmds = getSubcommands();
        String tmp = "";
        for (String cmd : cmds) {
            if (subCommandPermission.containsKey(cmd) && !sender.hasPermission(subCommandPermission.get(cmd)))
                continue;
            tmp += "\n    " + cmd + ":  " + getHelpContent("description", getHelpPrefix(), cmd);
            tmp += "\n    " + cmd + ":  " + getHelpContent("usage", getHelpPrefix(), cmd);
        }
        sender.sendMessage(tmp);
    }

    public static Player asPlayer(CommandSender target) {
        if (target instanceof Player) {
            return (Player) target;
        } else {
            throw new NotPlayerException();
        }
    }

    public void msg(CommandSender target, @LangKey String template, Object... args) {
        target.sendMessage(i18n.getFormatted(template, args));
    }

    public static ItemStack getItemInHand(CommandSender se) {
        if (se instanceof Player) {
            Player p = (Player) se;
            if (p.getInventory() != null) {
                ItemStack i = p.getInventory().getItemInMainHand();
                if (i != null && i.getType() != Material.AIR) {
                    return i;
                }
            }
            throw new NoItemInHandException(false);
        } else {
            throw new NotPlayerException();
        }
    }

    public static ItemStack getItemInOffHand(CommandSender se) {
        if (se instanceof Player) {
            Player p = (Player) se;
            if (p.getInventory() != null) {
                ItemStack i = p.getInventory().getItemInOffHand();
                if (i != null && i.getType() != Material.AIR) {
                    return i;
                }
            }
            throw new NoItemInHandException(true);
        } else {
            throw new NotPlayerException();
        }
    }

    @SuppressWarnings("unused")
    public static class Arguments {

        private List<String> parsedArguments = new ArrayList<>();
        private int index = 0;

        private Arguments() {
        }

        public static Arguments parse(String[] rawArg) {
            if (rawArg.length == 0) return new Arguments();
            String cmd = rawArg[0];
            for (int i = 1; i < rawArg.length; i++)
                cmd += " " + rawArg[i];

            List<String> cmdList = new ArrayList<>();
            boolean escape = false, quote = false;
            String tmp = "";
            for (int i = 0; i < cmd.length(); i++) {
                char chr = cmd.charAt(i);
                if (escape) {
                    if (chr == '\\' || chr == '`') tmp += chr;
                    else return null; // bad escape char
                    escape = false;
                } else if (chr == '\\') {
                    escape = true;
                } else if (chr == '`') {
                    if (quote) {
                        if (i + 1 == cmd.length() || cmd.charAt(i + 1) == ' ') {
                            cmdList.add(tmp);
                            tmp = "";
                            i++;
                            quote = false;
                        } else {
                            return null; //bad quote end
                        }
                    } else {
                        if (tmp.length() > 0) {
                            if (!tmp.matches("[a-zA-Z_]+[0-9a-zA-Z_]*:")) {//as a key:`value` pair
                                return null; // bad quote start
                            }
                        }
                        quote = true;
                    }
                } else if (chr == ' ') {
                    if (quote) {
                        tmp += ' ';
                    } else if (tmp.length() > 0) {
                        cmdList.add(tmp);
                        tmp = "";
                    }
                } else {
                    tmp += chr;
                }
            }
            if (tmp.length() > 0) cmdList.add(tmp);
            if (escape || quote) return null;

            Arguments ret = new Arguments();
            ret.parsedArguments = cmdList;
            return ret;
        }

        /**
         * @deprecated hard coded indexes are not recommended
         */
        public String at(int index) {
            return parsedArguments.get(index);
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

        // throw exception if no string found
        public String nextString() {
            String str = next();
            if (str == null) throw new BadCommandException("internal.error.no_more_string");
            return str;
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

        public <T extends Enum<T>> T nextEnum(Class<T> cls) {
            String str = next();
            if (str == null) throw new BadCommandException("internal.error.no_more_enum");
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

        public boolean nextBoolean() {
            String str = next();
            if (str == null) throw new BadCommandException("internal.error.no_more_bool");
            return Boolean.parseBoolean(str);
        }

        public Player nextPlayer() {
            String name = next();
            if (name == null) throw new BadCommandException("internal.error.no_more_player");
            Player p = Bukkit.getPlayer(name);
            if (p == null) throw new BadCommandException("internal.error.player_not_found", name);
            return p;
        }

        public Arguments nextAssert(String string) {
            String top = next();
            if (top == null && string == null) return this;
            if (top == null || string == null) throw new BadCommandException("internal.error.assert_fail", string, top);
            if (!string.equals(top)) throw new BadCommandException("internal.error.assert_fail", string, top);
            return this;
        }

        // Note: all `arg*()` functions will rearrange argument order
        // and mess up indexes. Thus, it's not recommended to use together with `at()` function

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
    }

    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubCommand {
        String value();

        String permission() default "";

        String attribute() default "";
    }

    // TODO: automatic call default subcommand when no match found
    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultCommand {
        String permission() default "";
    }
}
