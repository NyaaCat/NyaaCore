package cat.nyaa.nyaacore.cmdreceiver;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.LanguageRepository;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

public abstract class CommandReceiver implements CommandExecutor, TabCompleter {

    // Language class is passed in for message support
    private final ILocalizer i18n;
    // All subcommands
    private final Map<String, SubCommandInfo> subCommands = new HashMap<>();
    private final Map<String, String> subCommandAlias = new HashMap<>();
    // Default subcommand
    private SubCommandInfo defaultSubCommand = null;

    /**
     * @param plugin for logging purpose only
     * @param _i18n i18n
     */
    @SuppressWarnings("rawtypes")
    public CommandReceiver(Plugin plugin, ILocalizer _i18n) {
        if (plugin == null) throw new IllegalArgumentException();
        if (_i18n == null)
            _i18n = new LanguageRepository.InternalOnlyRepository(plugin);
        this.i18n = _i18n;

        // Collect all methods
        Class cls = getClass();
        Set<Method> allMethods = new HashSet<>();
        while (cls != null) {
            allMethods.addAll(Arrays.asList(cls.getDeclaredMethods()));
            cls = cls.getSuperclass();
        }

        // Collect all fields
        cls = getClass();
        Set<Field> allFields = new HashSet<>();
        while (cls != null) {
            allFields.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }

        Stream.concat(
                allMethods.stream().map(m -> parseSubCommandAnnotation(plugin, m)),
                allFields.stream().map(f -> parseSubCommandAnnotation(plugin, f))
        ).forEach(scInfo -> {
            if (scInfo == null) return;
            if (scInfo.name != null) {
                if (subCommands.containsKey(scInfo.name)) {
                    // TODO dup sub command
                }
                subCommands.put(scInfo.name, scInfo);

                if (scInfo.alias != null) {
                    for (String k : scInfo.alias) {
                        if (!k.isEmpty())
                            subCommandAlias.put(k, scInfo.name);
                    }
                }
            }

            if (scInfo.isDefault) {
                if (defaultSubCommand != null) {
                    // TODO dup default subcommand
                }
                defaultSubCommand = scInfo;
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static CommandReceiver newInstance(Class cls, Object arg1, Object arg2) throws ReflectiveOperationException {
        for (Constructor c : cls.getConstructors()) {
            if (c.getParameterCount() == 2 &&
                    c.getParameterTypes()[0].isAssignableFrom(arg1.getClass()) &&
                    c.getParameterTypes()[1].isAssignableFrom(arg2.getClass())) {
                return (CommandReceiver) c.newInstance(arg1, arg2);
            }
        }
        throw new NoSuchMethodException("no matching constructor found");
    }

    public static Player asPlayer(CommandSender target) {
        if (target instanceof Player) {
            return (Player) target;
        } else {
            throw new NotPlayerException();
        }
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

    // Scan recursively into parent class to find annotated methods when constructing

    /**
     * This prefix will be used to locate the correct manual item.
     * If the class is registered to bukkit directly, you should return a empty string.
     * If the class is registered through @SubCommand annotation, you should return the subcommand name.
     * If it's a nested subcommand, separate the prefixes using dot.
     *
     * @return the prefix
     */
    public abstract String getHelpPrefix();

    /**
     * @return should {@link CommandReceiver#acceptCommand(CommandSender, Arguments)} print the default "Success" message after executing a command
     */
    protected boolean showCompleteMessage() {
        return false;
    }

//
//    public List<String> getSubcommands() {
//        ArrayList<String> ret = new ArrayList<>();
//        ret.addAll(subCommands.keySet());
//        if (defaultSubCommand != null && defaultSubCommand.name == null) {
//            ret.add("<default>");
//        }
//        ret.sort(String::compareTo);
//        return ret;
//    }

    private SubCommandInfo getSubCommandInfo(Plugin plugin, AccessibleObject accessibleObject) {
        if (!(accessibleObject instanceof Field || accessibleObject instanceof Method)) {
            return null;
        }
        accessibleObject.setAccessible(true);
        SubCommand scAnno = accessibleObject.getAnnotation(SubCommand.class);
        if (scAnno == null) return null;

        boolean isDefault = scAnno.isDefaultCommand();
        boolean isField = accessibleObject instanceof Field;
        String subCommandName = scAnno.value().isEmpty() ? null : scAnno.value();
        String[] alias = scAnno.alias().length == 0 ? null : scAnno.alias();
        String permission = scAnno.permission().isEmpty() ? null : scAnno.permission();
        String tabCompleter = scAnno.tabCompleter().isEmpty() ? null : scAnno.tabCompleter();
        CommandReceiver fieldValue = null;
        Method method = null;
        Field field = null;
        Method tabCompleterMethod = null;

        if (alias != null) {
            if (subCommandName == null) return null;
            for (String s : alias) {
                if (s.isEmpty()) return null;
            }
        }

        if (!isDefault && subCommandName == null) {
            // not subcommand nor default command, remove the annotation
            return null;
        }

        if (tabCompleter != null) {
            Class<?> declaringClass = isField ? ((Field) accessibleObject).getDeclaringClass() : ((Method) accessibleObject).getDeclaringClass();
            try {
                tabCompleterMethod = declaringClass.getDeclaredMethod(scAnno.tabCompleter(), CommandSender.class, Arguments.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (isField) {//Field
            field = (Field) accessibleObject;
            Class<?> fieldType = field.getType();
            if (!CommandReceiver.class.isAssignableFrom(fieldType)) {
                return null; // incorrect field type
            }
            if (tabCompleterMethod != null) {
                return null; // field-based subcommand does not need method-based tabcompletion
            }
            try {
                fieldValue = newInstance(fieldType, plugin, i18n);
                ((Field) accessibleObject).set(this, fieldValue);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }

        } else {//Method
            method = (Method) accessibleObject;
            Class<?>[] params = method.getParameterTypes();
            if (!(params.length == 2 &&
                    params[0] == CommandSender.class &&
                    params[1] == Arguments.class)) {
                return null; // incorrect method signature
            }
        }
        return new SubCommandInfo(subCommandName, alias, permission, isField, method, field, fieldValue, isDefault, tabCompleterMethod);
    }

    private SubCommandInfo parseSubCommandAnnotation(Plugin plugin, AccessibleObject accessibleObject) {
        SubCommand scAnno = accessibleObject.getAnnotation(SubCommand.class);
        if (scAnno == null) return null;
        SubCommandInfo result = getSubCommandInfo(plugin, accessibleObject);
        if (result == null) {
            plugin.getLogger().warning(i18n.getFormatted("internal.error.bad_subcommand", accessibleObject.toString()));
        }
        return result;
    }

    /*
     * Code path looks like this:
     * - Bukkit => CmdRecv:onCommand => CmdRecv:acceptCommand => SubCmdRecv:acceptCommand => SubCmdRecv:commandMethod
     * <p>
     * Determine subcommand method or class and Exception collection.
     * Can be override for finer subcommand routing
     * <p>
     * Subcommand execution search order:
     * 1. {@link CommandReceiver#subCommands}
     * 2. {@link CommandReceiver#defaultSubCommand}
     * 3. {@link CommandReceiver#printHelp(CommandSender, Arguments)}
     */

    protected Set<String> getSubCommands() {
        return Collections.unmodifiableSet(subCommands.keySet());
    }

    // Only directly registered command handler need this
    // acceptCommand() will be called directly in subcommand classes
    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Arguments cmd = Arguments.parse(args, sender);
        if (cmd == null) return false;
        acceptCommand(sender, cmd);
        return true;
    }

    /*
     * The code path looks like this:
     * - Bukkit => CmdRecv:onTabComplete => CmdRecv:acceptTabComplete => SubCmdRecv:acceptTabComplete => SubCmdRecv:tabCompleteMethod
     * <p>
     * Subcommand tab completion search order:
     * 1. {@link CommandReceiver#subCommands}
     * 2. {@link CommandReceiver#defaultSubCommand}.callTabCompletion
     * 3. default builtin completion logic
     * <p>
     */

    /**
     * @param sender who run the command
     * @param cmd    the command, or part of the command
     */
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        String CommandTop = cmd.top();
        String subCommand = getSubCommandName(CommandTop);
        try {
            boolean subclass_may_print_success_msg;
            try {
                if (subCommand != null && subCommands.containsKey(subCommand)) {
                    cmd.next(); // drop the first parameter
                    subclass_may_print_success_msg = subCommands.get(subCommand).isField;
                    subCommands.get(subCommand).callCommand(sender, cmd);
                } else if (defaultSubCommand != null) {
                    subclass_may_print_success_msg = defaultSubCommand.isField;
                    defaultSubCommand.callCommand(sender, cmd);
                } else {
                    subclass_may_print_success_msg = true;
                    printHelp(sender, cmd);
                }

                if (!subclass_may_print_success_msg && showCompleteMessage()) {
                    msg(sender, "internal.info.command_complete");
                }
            } catch (ReflectiveOperationException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                else
                    throw new RuntimeException("Failed to invoke subcommand", ex);
            }

        } catch (NotPlayerException ex) {
            msg(sender, "internal.error.not_player");
        } catch (NoItemInHandException ex) {
            msg(sender, ex.isOffHand ? "internal.error.no_item_offhand" : "internal.error.no_item_hand");
        } catch (BadCommandException ex) {
            String msg = ex.getMessage();
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
                    getHelpContent("usage", getHelpPrefix(), CommandTop));
        } catch (NoPermissionException ex) {
            msg(sender, "internal.error.no_required_permission", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            msg(sender, "internal.error.command_exception");
        }
    }

    // Only directly registered command handler need this
    // acceptTabComplete() will be called directly in subcommand classes
    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        try {
            Arguments cmd = Arguments.parsePreserveLastBlank(args, sender);
            if (cmd == null) return null;
            return acceptTabComplete(sender, cmd);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @param sender who run the command
     * @param args   the command, or part of the command
     * @return tab completion candidates
     */
    public List<String> acceptTabComplete(CommandSender sender, Arguments args) {
        String cmd = args.top();
        if (cmd == null) return null;
        boolean isPartial = args.remains() == 1;

        if (isPartial) {
            // ask default command
            // list all matching subcommands
            List<String> ret = null;
            if (defaultSubCommand != null) ret = defaultSubCommand.callTabComplete(sender, args);
            if (ret == null) ret = new ArrayList<>();
            final String cmd_prefix = cmd;
            Set<String> allSubCommandsName = new HashSet<>();
            allSubCommandsName.addAll(subCommandAlias.keySet());
            allSubCommandsName.addAll(subCommands.keySet());
            List<String> subcommands =allSubCommandsName.stream().filter(s -> s.startsWith(cmd_prefix)).sorted().toList();
            ret.addAll(subcommands);
            return ret;
        } else {
            // goto subcommand if exact match found
            // otherwise ask default command
            String subCommandName = getSubCommandName(cmd);
            if (subCommandName !=null) {
                args.next();
                return subCommands.get(subCommandName).callTabComplete(sender, args);
            } else if (defaultSubCommand != null) {
                return defaultSubCommand.callTabComplete(sender, args);
            } else {
                return null;
            }
        }
    }

    @Nullable
    private String getSubCommandName(@Nullable String str) {
        if (str == null) return null;
        if (subCommands.containsKey(str)) return str;
        if (subCommandAlias.containsKey(str)) {
            return subCommandAlias.get(str);
        }
        return null;
    }
    @Nullable
    private String[] getSubCommandAlias(@Nullable SubCommandInfo subCommandInfo) {
        if(subCommandInfo == null)return null;
        if(subCommandInfo.alias == null)return null;
        ArrayList<String> list = Lists.newArrayList(subCommandInfo.alias);
        list.removeIf(String::isEmpty);
        if(list.size()<=0)return null;
        return list.toArray(new String[0]);
    }

    private String getHelpContent(String type, String... subkeys) {
        String key = "manual";
        for (String s : subkeys) {
            if (s != null && s.length() > 0)
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
        List<String> cmds = new ArrayList<>(subCommands.keySet());
        cmds.sort(Comparator.naturalOrder());

        String tmp = "";
        for (String cmd : cmds) {
            if (!subCommands.get(cmd).hasPermission(sender)) continue;
            tmp += "\n    " + cmd + ":  " + getHelpContent("description", getHelpPrefix(), cmd) + ChatColor.RESET;
            tmp += "\n    " + cmd + ":  " + getHelpContent("usage", getHelpPrefix(), cmd) + ChatColor.RESET;
        }

        if (defaultSubCommand != null && defaultSubCommand.hasPermission(sender)) {
            String cmd = "<default>";
            tmp += "\n    " + cmd + ":  " + getHelpContent("description", getHelpPrefix(), cmd) + ChatColor.RESET;
            tmp += "\n    " + cmd + ":  " + getHelpContent("usage", getHelpPrefix(), cmd) + ChatColor.RESET;
        }
        sender.sendMessage(tmp);
    }

    public void msg(CommandSender target, String template, Object... args) {
        target.sendMessage(i18n.getFormatted(template, args));
    }

    private class SubCommandInfo {
        final String name; // default command can have this be null
        final String permission; // if none then no permission required
        final Method tabCompleter;
        final boolean isField; // isField? field : method;
        final Method method;
        final Field field;
        final CommandReceiver fieldValue;
        final boolean isDefault;
        public String[] alias;

        SubCommandInfo(String name, String[] alias, String permission, boolean isField, Method method, Field field, CommandReceiver fieldValue, boolean isDefault, Method tabCompleter) {
            if (name == null && !isDefault) throw new IllegalArgumentException();
            if (name == null && alias != null) throw new IllegalArgumentException();
            if (isField && !(method == null && field != null && fieldValue != null))
                throw new IllegalArgumentException();
            if (!isField && !(method != null && field == null && fieldValue == null))
                throw new IllegalArgumentException();
            if (isField && tabCompleter != null) {
                throw new IllegalArgumentException();
            }
            this.name = name;
            this.permission = permission;
            this.isField = isField;
            this.method = method;
            this.field = field;
            this.fieldValue = fieldValue;
            this.isDefault = isDefault;
            this.tabCompleter = tabCompleter;
        }

        void callCommand(CommandSender sender, Arguments args) throws IllegalAccessException, InvocationTargetException {
            if (permission != null && !sender.hasPermission(permission)) {
                throw new NoPermissionException(permission);
            }
            if (isField) {
                fieldValue.acceptCommand(sender, args);
            } else {
                method.invoke(CommandReceiver.this, sender, args);
            }
        }

        @SuppressWarnings("unchecked")
        List<String> callTabComplete(CommandSender sender, Arguments args) {
            if (permission != null && !sender.hasPermission(permission)) {
                return null;
            }
            if (isField) {
                return fieldValue.acceptTabComplete(sender, args);
            } else if (tabCompleter != null) {
                try {
                    return (List<String>) tabCompleter.invoke(CommandReceiver.this, sender, args);
                } catch (ReflectiveOperationException ex) {
                    return null;
                }
            } else {
                return null;
            }
        }

        boolean hasPermission(CommandSender sender) {
            if (permission == null) return true;
            return sender.hasPermission(permission);
        }
    }
}
