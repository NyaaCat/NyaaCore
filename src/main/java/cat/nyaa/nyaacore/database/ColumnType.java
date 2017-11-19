package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

public enum ColumnType {
    TEXT,
    INTEGER, // use long in java
    REAL; //use double in java

    /**
     * Determine database type for the java type
     * Note: for FIELD_PARSE, database type will always be string
     *       because it use toString() and fromString/parse().
     *
     * @return determined database type
     */
    public static ColumnType getType(ColumnAccessMethod accessMethod, Class cls) {
        if (accessMethod == ColumnAccessMethod.FIELD_PARSE) return TEXT;
        if (cls == long.class || cls == Long.class) return INTEGER;
        if (cls == boolean.class || cls == Boolean.class) return INTEGER;
        if (cls == double.class || cls == Double.class) return REAL;
        if (cls == String.class) return TEXT;
        if (cls.isEnum()) return TEXT;
        if (cls == ItemStack.class) return TEXT;
        throw new IllegalArgumentException("Unsupported type");
    }

    /**
     * Convert a java object to a database object
     * Actually this method can be static because the parameter itself
     * should bring enough information for determining the output type
     *
     * @param raw java object
     * @return an object of following types: String/Long/Double
     */
    public Object toDatabaseType(Object raw) {
        Class cls = raw.getClass();
        if (this == TEXT) {
            if (cls == String.class) return raw;
            if (cls.isEnum()) return ((Enum) raw).name();
            if (cls == ItemStack.class) return ItemStackUtils.itemToBase64((ItemStack) raw);
        } else if (this == INTEGER) {
            if (cls == Long.class) return raw;
            if (cls == Boolean.class) return raw == Boolean.TRUE ? 1L : 0L;
        } else if (this == REAL) {
            if (cls == Double.class) return raw;
        } else {
            throw new RuntimeException("Invalid ColumnType");
        }
        throw new RuntimeException("Java object cannot be cast to designated SQL type: " + this.name());
    }

    /**
     * Convert a database object to a java object
     * Note: the database objects may not be types of: String/Long/Double but should be compatible
     * TODO: figure out what types they are
     *
     * Actually this method can be static because javaTypeClass itself
     * should bring enough information for determining how to parse sqlObject
     *
     * @param sqlObject object from database query result
     * @param javaTypeClass the desired java type
     * @return an object of type javaTypeClass
     */
    public Object toJavaType(Object sqlObject, Class javaTypeClass) {
        if (this == TEXT) {
            String str = (String) sqlObject;
            if (javaTypeClass == String.class) return str;
            if (javaTypeClass.isEnum()) return Enum.valueOf(javaTypeClass, str);
            if (javaTypeClass == ItemStack.class) return ItemStackUtils.itemFromBase64(str);
        } else if (this == INTEGER) {
            Long num = ((Number) sqlObject).longValue();
            if (javaTypeClass == Long.class || javaTypeClass == long.class) return num;
            if (javaTypeClass == Boolean.class || javaTypeClass == Boolean.TYPE) return num == 1L;
        } else if (this == REAL) {
            Double num = ((Number) sqlObject).doubleValue();
            if (javaTypeClass == Double.class || javaTypeClass == double.class) return num;
        } else {
            throw new RuntimeException("Invalid ColumnType");
        }
        throw new RuntimeException("SQL object cannot be cast to java type: " + this.name() + " -> " + javaTypeClass.getName());
    }
}
