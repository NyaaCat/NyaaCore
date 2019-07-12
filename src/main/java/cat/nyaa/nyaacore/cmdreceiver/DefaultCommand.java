package cat.nyaa.nyaacore.cmdreceiver;

import cat.nyaa.nyaacore.ILocalizer;
import org.bukkit.plugin.Plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: automatic call default subcommand when no match found

/**
 * For signature requirements, see {@link SubCommand}
 * A sub command can be either:
 * a method with such signature:
 * void methodName(CommandSender sender, Arguments args);
 * or a field whose type has such constructor:
 * TypeClass(? extends {@link org.bukkit.plugin.Plugin} plugin, ? extends {@link cat.nyaa.nyaacore.ILocalizer} i18n);
 * and don't forget to call {@link CommandReceiver#CommandReceiver(Plugin, ILocalizer)} in the constructor.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultCommand {
    String permission() default "";
}
