package cat.nyaa.nyaacore.database;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A database column can be:
 * 1. A field of acceptable type: long/double/bool/Enum/String/ItemStack
 * 2. A field can be serialized/deserialized using toString() and fromString()/parse() (e.g. ZonedDateTime)
 * 3. A pair of getter/setter returning/accepting type listed in (1)
 */
public class ColumnStructure {
    final String name;
    final TableStructure table;
    final boolean isPrimary;

    final Field field;      // for FIELD or FIELD_PARSER
    final Class fieldType; // this is native java type
    final Method fieldParser; // for FIELD_PARSER
    final Method setter; // for METHOD
    final Method getter; // for METHOD

    final ColumnAccessMethod accessMethod;
    final ColumnType columnType; // corresponding SQL type

    /**
     * Constructor for field based table columns
     */
    public ColumnStructure(TableStructure table, Field dataField, DataColumn anno) {
        if (anno == null) throw new IllegalArgumentException();
        this.table = table;
        String name = anno.value();
        if ("".equals(name)) name = dataField.getName();
        this.name = name;
        this.isPrimary = dataField.getDeclaredAnnotation(PrimaryKey.class) != null;
        dataField.setAccessible(true);
        field = dataField;
        fieldType = field.getType();
        setter = null;
        getter = null;
        fieldParser = getParserMethod(dataField.getType());
        if (fieldParser == null) {
            accessMethod = ColumnAccessMethod.FIELD;
            columnType = ColumnType.getType(ColumnAccessMethod.FIELD, field.getType());
        } else {
            accessMethod = ColumnAccessMethod.FIELD_PARSE;
            columnType = ColumnType.getType(ColumnAccessMethod.FIELD_PARSE, field.getType());
        }
    }

    /**
     * Check if given type has valid `parse` static method
     */
    private Method getParserMethod(Class<?> cls) {
        for (Method m : cls.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                if ("parse".equals(m.getName()) || "fromString".equals(m.getName())) {
                    if (m.getReturnType() == cls) {
                        if (m.getParameterCount() == 1) {
                            if (m.getParameterTypes()[0].isAssignableFrom(String.class)) {
                                return m;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Constructor for method based table columns
     */
    public ColumnStructure(TableStructure table, Method dataMethod, DataColumn anno) {
        if (anno == null) throw new IllegalArgumentException();
        this.table = table;
        String methodName = dataMethod.getName();
        if (!methodName.startsWith("get") && !methodName.startsWith("set"))
            throw new IllegalArgumentException("Method is neither a setter nor a getter: " + dataMethod.toString());
        String methodSuffix = methodName.substring(3);
        String name = ("".equals(anno.value())) ? methodSuffix : anno.value();
        Class methodType;
        if (methodName.startsWith("get")) {
            methodType = dataMethod.getReturnType();
        } else {
            methodType = dataMethod.getParameterCount() == 1 ? dataMethod.getParameterTypes()[0] : null;
        }
        if (methodType == null || methodType == Void.class || methodType == Void.TYPE)
            throw new IllegalArgumentException("Cannot determine getter/setter type: " + dataMethod.toString());
        //if (methodType != String.class && methodType != Long.class && methodType != Double.class)
        //   throw new IllegalArgumentException("Only three types are supported for getter/setter columns: String/Long/Double");

        Method getter, setter;
        try {
            getter = dataMethod.getDeclaringClass().getDeclaredMethod("get" + methodSuffix);
            setter = dataMethod.getDeclaringClass().getDeclaredMethod("set" + methodSuffix, methodType);
            if (getter.getParameterCount() != 0 || getter.getReturnType() != methodType || Modifier.isStatic(getter.getModifiers()))
                throw new RuntimeException("getter signature mismatch");
            if (setter.getParameterCount() != 1 || setter.getParameterTypes()[0] != methodType ||
                    (setter.getReturnType() != Void.class && setter.getReturnType() != Void.TYPE) ||
                    Modifier.isStatic(setter.getModifiers()))
                throw new RuntimeException("setter signature mismatch");
            boolean primary = getter.getDeclaredAnnotation(PrimaryKey.class) != null;
            primary |= setter.getDeclaredAnnotation(PrimaryKey.class) != null;
            getter.setAccessible(true);
            setter.setAccessible(true);
            this.isPrimary = primary;
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        this.columnType = ColumnType.getType(ColumnAccessMethod.METHOD, methodType);
        this.accessMethod = ColumnAccessMethod.METHOD;
        this.getter = getter;
        this.setter = setter;
        this.field = null;
        this.fieldParser = null;
        this.fieldType = methodType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public TableStructure getTable() {
        return table;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public ColumnAccessMethod getAccessMethod() {
        return accessMethod;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public String getTableCreationScheme() {
        String ret = String.format("%s %s NOT NULL", name, columnType.name());
        if (isPrimary) ret += " PRIMARY KEY";
        return ret;
    }

    /**
     * Fetch the from the field/getter and return a SQL-typed object
     * Return type must be one of Long/Double/String
     */
    @SuppressWarnings("deprecation")
    public Object fetchFromObject(Object javaObject) {
        Object raw;
        try {
            switch (accessMethod) {
                case FIELD:
                case FIELD_PARSE:
                    raw = field.get(javaObject);
                    break;
                case METHOD:
                    raw = getter.invoke(javaObject);
                    break;
                default:
                    throw new RuntimeException("Invalid accessMethod");
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }

        if (raw == null) {
            if (!isPrimary)
                throw new IllegalArgumentException(String.format("NULL is not allowed for column: %s#%s ", table.tableName, name));
            return null;
        }
        if (accessMethod == ColumnAccessMethod.FIELD_PARSE) return raw.toString();
        return columnType.toDatabaseType(raw);
    }

    @SuppressWarnings("deprecation")
    public void saveToObject(Object javaObject, Object sqlValueObject) {
        try {
            Object raw;
            if (accessMethod == ColumnAccessMethod.FIELD_PARSE) {
                raw = fieldParser.invoke(null, (String) sqlValueObject);
                // TODO assert raw != null
            } else {
                raw = columnType.toJavaType(sqlValueObject, fieldType);
            }
            if (raw != null && !fieldType.isInstance(raw)) {
                throw new RuntimeException(String.format("%s(%s) is not a valid Java object for column %s#%s",
                        raw.getClass(), raw.toString(), table.tableName, name));
            }
            switch (accessMethod) {
                case FIELD:
                case FIELD_PARSE:
                    field.set(javaObject, raw);
                    break;
                case METHOD:
                    setter.invoke(javaObject, raw);
                    break;
                default:
                    throw new RuntimeException("Invalid accessMethod");
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * ColumnType class does not handle FIELD_PARSE info.
     */
    @SuppressWarnings("deprecation")
    public Object toDatabaseType(Object raw) {
        if (raw == null) {
            if (!isPrimary)
                throw new IllegalArgumentException(String.format("NULL is not allowed for column: %s#%s ", table.tableName, name));
            return null;
        }
        if (!fieldType.isInstance(raw)) {
            throw new RuntimeException(String.format("%s(%s) is not a valid Java object for column %s#%s",
                    raw.getClass(), raw.toString(), table.tableName, name));
        }
        if (accessMethod == ColumnAccessMethod.FIELD_PARSE) raw = raw.toString();
        return columnType.toDatabaseType(raw);
    }

    @SuppressWarnings("deprecation")
    public Object toJavaType(Object sqlObject) {
        if (sqlObject == null) return null;
        try {
            if (accessMethod == ColumnAccessMethod.FIELD_PARSE) return fieldParser.invoke(null, (String) sqlObject);
            // TODO assert return != null
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        return columnType.toJavaType(sqlObject, fieldType);
    }
}
