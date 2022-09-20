package cat.nyaa.nyaacore.orm;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLType;
import java.util.HashMap;
import java.util.Map;

import static java.sql.JDBCType.*;

/*
 * There are all accepted java types:
 *   1. bool/Boolean  => INTEGER  [true=1, false=0]
 *   2. int/Integer   => INTEGER  [no conversion]
 *   3. long/Long     => BIGINT   [no conversion]
 *   4. float/Float   => FLOAT    [no conversion]
 *   5. double/Double => DOUBLE   [no conversion]
 *   6. String        => MEDIUMTEXT     [no conversion]
 *   7. Enum          => MEDIUMTEXT     [name() and valueOf()]
 *   8. ItemStack     => MEDIUMTEXT     [nbt(de)serializebase64()]
 *   9. Any type can be serialized/deserialized using toString() and fromString()/parse() (e.g. ZonedDateTime)
 *                    => MEDIUMTEXT     [toString() and fromString()/parse()]
 */
public class DataTypeMapping {

    private static final Map<Class, IDataTypeConverter> cached_converters = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public static boolean isStaticParsingType(Class cls) {
        for (Method m : cls.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                if ("parse".equals(m.getName()) || "fromString".equals(m.getName())) {
                    if (m.getReturnType() == cls) {
                        if (m.getParameterCount() == 1) {
                            if (m.getParameterTypes()[0].isAssignableFrom(String.class)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static IDataTypeConverter getDataTypeConverter(Class cls) {
        if (cls == boolean.class || cls == Boolean.class) return BooleanConverter.INSTANCE;
        if (cls == int.class || cls == Integer.class) return IntegerConverter.INSTANCE;
        if (cls == long.class || cls == Long.class) return LongConverter.INSTANCE;
        if (cls == float.class || cls == Float.class) return FloatConverter.INSTANCE;
        if (cls == double.class || cls == Double.class) return DoubleConverter.INSTANCE;
        if (cls == String.class) return StringConverter.INSTANCE;
        if (cls.isEnum()) {
            IDataTypeConverter cvt = cached_converters.get(cls);
            if (cvt == null) {
                cvt = new EnumConverter(cls);
                cached_converters.put(cls, cvt);
            }
            return cvt;
        }
        if (cls == ItemStack.class) return ItemStackConverter.INSTANCE;
        if (isStaticParsingType(cls)) {
            IDataTypeConverter cvt = cached_converters.get(cls);
            if (cvt == null) {
                cvt = new StaticParsingTypeConverter(cls);
                cached_converters.put(cls, cvt);
            }
            return cvt;
        }
        if (cls == byte[].class) throw new NotImplementedException("byte[] is not yet supported");
        throw new IllegalArgumentException("Not an acceptable type: " + cls);
    }

    /**
     * Convert one particular type of java objects to/from the java representation of SQL type
     *
     * @param <T> Java type
     */
    public interface IDataTypeConverter<T> {
        default Object toSqlType(T obj) {
            return obj;
        }

        T toJavaType(Object obj);

        SQLType getSqlType();
    }

    public static class BooleanConverter implements IDataTypeConverter<Boolean> {
        public static BooleanConverter INSTANCE = new BooleanConverter();

        @Override
        public Object toSqlType(Boolean obj) {
            if (obj == null) return null;
            return obj ? 1 : 0;
        }

        @Override
        public Boolean toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof Number) {
                return ((Number) obj).intValue() == 1;
            } else {
                throw new IllegalArgumentException("Expecting number but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return INTEGER;
        }
    }

    public static class IntegerConverter implements IDataTypeConverter<Integer> {
        public static IntegerConverter INSTANCE = new IntegerConverter();

        @Override
        public Integer toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            } else {
                throw new IllegalArgumentException("Expecting number but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return INTEGER;
        }
    }

    public static class LongConverter implements IDataTypeConverter<Long> {
        public static LongConverter INSTANCE = new LongConverter();

        @Override
        public Long toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            } else {
                throw new IllegalArgumentException("Expecting number but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return BIGINT;
        }
    }

    public static class FloatConverter implements IDataTypeConverter<Float> {
        public static FloatConverter INSTANCE = new FloatConverter();

        @Override
        public Float toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof Number) {
                return ((Number) obj).floatValue();
            } else {
                throw new IllegalArgumentException("Expecting number but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return FLOAT;
        }
    }

    public static class DoubleConverter implements IDataTypeConverter<Double> {
        public static DoubleConverter INSTANCE = new DoubleConverter();

        @Override
        public Double toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            } else {
                throw new IllegalArgumentException("Expecting number but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return DOUBLE;
        }
    }

    public static class StringConverter implements IDataTypeConverter<String> {
        public static StringConverter INSTANCE = new StringConverter();

        @Override
        public String toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof String) {
                return (String) obj;
            } else {
                throw new IllegalArgumentException("Expecting string but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return VARCHAR;
        }
    }

    public static class EnumConverter<E extends Enum<E>> implements IDataTypeConverter<E> {
        private final Class<E> enumClass;

        public EnumConverter(Class<E> enumClass) {
            if (!enumClass.isEnum()) {
                throw new IllegalArgumentException("Class is not an enum: " + enumClass);
            }
            this.enumClass = enumClass;
        }

        @Override
        public Object toSqlType(E obj) {
            if (obj == null) return null;
            return obj.name();
        }

        @Override
        public E toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof String) {
                return Enum.valueOf(enumClass, (String) obj);
            } else {
                throw new IllegalArgumentException("Expecting string but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return VARCHAR;
        }
    }

    public static class ItemStackConverter implements IDataTypeConverter<ItemStack> {
        public static ItemStackConverter INSTANCE = new ItemStackConverter();

        @Override
        public Object toSqlType(ItemStack obj) {
            if (obj == null) return null;
            return ItemStackUtils.itemToBase64(obj);
        }

        @Override
        public ItemStack toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof String) {
                ItemStack result = ItemStackUtils.itemFromBase64((String) obj);
                if (result == null) {
                    Bukkit.getLogger().warning("not a valid itemstack value in database:" + obj);
                }
                return result;
            } else {
                throw new IllegalArgumentException("Expecting string but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return VARCHAR;
        }
    }

    public static class StaticParsingTypeConverter<T> implements IDataTypeConverter<T> {
        private final Class<T> cls;
        private final Method parseMethod;

        public StaticParsingTypeConverter(Class<T> cls) {
            this.cls = cls;
            for (Method m : cls.getMethods()) {
                if (Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                    if ("parse".equals(m.getName()) || "fromString".equals(m.getName())) {
                        if (m.getReturnType() == cls) {
                            if (m.getParameterCount() == 1) {
                                if (m.getParameterTypes()[0].isAssignableFrom(String.class)) {
                                    parseMethod = m;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            throw new IllegalArgumentException("Cannot find valid parse/fromString method in class " + cls.getName());
        }

        @Override
        public Object toSqlType(T obj) {
            if (obj == null) return null;
            return obj.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T toJavaType(Object obj) {
            if (obj == null) return null;
            if (obj instanceof String) {
                try {
                    return (T) parseMethod.invoke(null, obj);
                } catch (ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                throw new IllegalArgumentException("Expecting string but received " + obj);
            }
        }

        @Override
        public SQLType getSqlType() {
            return VARCHAR;
        }
    }
}
