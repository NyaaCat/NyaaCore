package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

/*
 * TODO this enum may be replaced by {@link java.sql.Types}
 */
public enum ColumnType {
    MEDIUMTEXT,
    INTEGER, // use long in java
    REAL; //use double in java

    /**
     * Determine database type for the java type
     * Note: for FIELD_PARSE, database type will always be string
     * because it use toString() and fromString/parse().
     *
     * @return determined database type
     */
    public static ColumnType getType(ColumnAccessMethod accessMethod, Class<?> cls) {
        if (accessMethod == ColumnAccessMethod.FIELD_PARSE) return MEDIUMTEXT;
        if (cls == long.class || cls == Long.class) return INTEGER;
        if (cls == boolean.class || cls == Boolean.class) return INTEGER;
        if (cls == double.class || cls == Double.class) return REAL;
        if (cls == String.class) return MEDIUMTEXT;
        if (cls.isEnum()) return MEDIUMTEXT;
        if (cls == ItemStack.class) return MEDIUMTEXT;
        throw new IllegalArgumentException("Unsupported type");
    }

    /**
     * Convert a java object to a database object
     * Actually this method can be static because the parameter itself
     * should bring enough information for determining the output type
     *
     * @param raw java object
     * @return an object of following types: String/Long/Double
     * @deprecated cannot handle FIELD_PARSE access type. Use {@link ColumnStructure#toDatabaseType(Object)}
     */
    @Deprecated
    public Object toDatabaseType(Object raw) {
        if(raw == null) return null;
        Class<?> cls = raw.getClass();
        if (this == MEDIUMTEXT) {
            if (cls == String.class) return raw;
            if (cls.isEnum()) return ((Enum) raw).name();
            if (ItemStack.class.isAssignableFrom(cls)) return ItemStackUtils.itemToBase64((ItemStack) raw);
        } else if (this == INTEGER) {
            if (cls == Boolean.class) return (Boolean) raw ? 1L : 0L;
            if (cls == Long.class || cls == Integer.class || cls == Short.class || cls == Byte.class)
                return ((Number) raw).longValue();
        } else if (this == REAL) {
            if (cls == Double.class || cls == Float.class)
                return ((Number) raw).doubleValue();
        } else {
            throw new RuntimeException("Invalid ColumnType");
        }
        throw new RuntimeException(String.format("Java object %s(%s) cannot be cast to designated SQL type %s", cls, raw, this.name()));
    }

    /**
     * Convert a database object to a java object
     * Note: the database objects may not be types of: String/Long/Double but should be compatible
     * TODO: figure out what types they are
     * <p>
     * Actually this method can be static because javaTypeClass itself
     * should bring enough information for determining how to parse sqlObject
     *
     * @param sqlObject     object from database query result
     * @param javaTypeClass the desired java type
     * @return an object of type javaTypeClass
     * @deprecated cannot handle FIELD_PARSE access type. Use {@link ColumnStructure#toJavaType(Object)}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Deprecated
    public Object toJavaType(Object sqlObject, Class javaTypeClass) {
        if (sqlObject == null) {
            if (javaTypeClass.isPrimitive()) {
                throw new RuntimeException("primitive types do not accept NULL values");
            } else {
                return null;
            }
        }
        if (this == MEDIUMTEXT) {
            String str = (String) sqlObject;
            if (javaTypeClass == String.class) return str;
            if (javaTypeClass.isEnum()) return Enum.valueOf(javaTypeClass, str);
            if (javaTypeClass == ItemStack.class) return ItemStackUtils.itemFromBase64(str);
        } else if (this == INTEGER) {
            Long num = ((Number) sqlObject).longValue();
            if (javaTypeClass == Long.class || javaTypeClass == long.class) return num;
            if (javaTypeClass == Boolean.class || javaTypeClass == Boolean.TYPE)
                return num.equals(1L);
        } else if (this == REAL) {
            Double num = ((Number) sqlObject).doubleValue();
            if (javaTypeClass == Double.class || javaTypeClass == double.class) return num;
        } else {
            throw new RuntimeException("Invalid ColumnType");
        }
        throw new RuntimeException("SQL object cannot be cast to java type: " + this.name() + " -> " + javaTypeClass.getName());
    }
}
