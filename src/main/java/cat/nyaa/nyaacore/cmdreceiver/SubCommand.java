package cat.nyaa.nyaacore.cmdreceiver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SubCommands ARE CASE-SENSITIVE
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {
    String value() default "";

    String[] alias() default {};

    boolean isDefaultCommand() default false;

    String permission() default "";

    String tabCompleter() default "";
}
