package cat.nyaa.nyaacore.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A partial copy of javax.persistence.Column
 * because I don't like shadow whole JPA
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default ""; // column name, use field name if empty

    String columnDefinition() default ""; // if this is not empty, everything else except name() will be ignored

    boolean nullable() default false;

    boolean unique() default false;

    boolean primary() default false;

    boolean autoIncrement() default false;

    int length() default -1;
}
