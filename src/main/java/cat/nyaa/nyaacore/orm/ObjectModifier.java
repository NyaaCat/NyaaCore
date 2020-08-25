package cat.nyaa.nyaacore.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Access a Java object like a database record
 * One type needs only one ObjectModifier instance and the instance can be used
 * on many different objects.
 * All ObjectModifier instances are stored permanently in {@link ObjectModifier#structured_tables}.
 *
 * @param <T> type of the java object, must have default constructor
 */
public class ObjectModifier<T> {
    /* class -> TableStructure cache */
    private static final Map<Class<?>, ObjectModifier<?>> structured_tables = new HashMap<>();
    // Java object type info
    public final Class<T> clz;
    public final Constructor<T> ctor;
    // SQL table info
    public final String tableName;
    public final List<String> orderedColumnName = new ArrayList<>();
    public final Map<String, ObjectFieldModifier> columns = new HashMap<>();
    public final String primaryKey; // null if no primary key

    private ObjectModifier(Class<T> tableClass) throws NoSuchMethodException {
        Table annotationTable = tableClass.getDeclaredAnnotation(Table.class);

        this.clz = tableClass;
        Constructor<T> ctor;
        try {
            ctor = tableClass.getConstructor();
        } catch (NoSuchMethodException e) {
            ctor = tableClass.getDeclaredConstructor();
        }
        this.ctor = ctor;
        this.ctor.setAccessible(true);
        if (annotationTable == null || annotationTable.value().isEmpty()) {
            this.tableName = tableClass.getSimpleName();
        } else {
            this.tableName = annotationTable.value();
        }

        String pkColumn = null;

        // load all the fields
        for (Field f : tableClass.getDeclaredFields()) {
            Column columnAnnotation = f.getAnnotation(Column.class);
            if (columnAnnotation == null) continue;
            ObjectFieldModifier structure = new ObjectFieldModifier(this, f, columnAnnotation);
            if (columns.containsKey(structure.getName()))
                throw new RuntimeException("Duplicated column name: " + structure.getName());
            if (structure.primary) {
                if (pkColumn != null) throw new RuntimeException("Duplicated primary key at: " + f.getName());
                pkColumn = structure.getName();
            }
            columns.put(structure.getName(), structure);
        }

        // load all the getter/setter
        for (Method m : tableClass.getDeclaredMethods()) {
            Column columnAnnotation = m.getAnnotation(Column.class);
            if (columnAnnotation == null) continue;
            ObjectFieldModifier structure = new ObjectFieldModifier(this, m, columnAnnotation);
            if (columns.containsKey(structure.getName()))
                throw new RuntimeException("Duplicated column name: " + structure.getName());
            if (structure.primary) {
                if (pkColumn != null) throw new RuntimeException("Duplicated primary key at: " + m.getName());
                pkColumn = structure.getName();
            }
            columns.put(structure.getName(), structure);
        }

        primaryKey = pkColumn;
        orderedColumnName.addAll(columns.keySet());
        orderedColumnName.sort(String::compareTo);
    }

    @SuppressWarnings("unchecked")
    public static <X> ObjectModifier<X> fromClass(Class<X> cls) {
        if (structured_tables.containsKey(cls)) return (ObjectModifier<X>) structured_tables.get(cls);
        ObjectModifier<X> ts;
        try {
            ts = new ObjectModifier<>(cls);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        structured_tables.put(cls, ts);
        return ts;
    }






    /* ********** *
     * class info *
     * ********** */

    public Class<T> getJavaClass() {
        return clz;
    }

    /**
     * @return table name, nullable
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return a readonly list of column names
     */
    public List<String> getColNames() {
        return Collections.unmodifiableList(orderedColumnName);
    }

    /**
     * Get primary key column name
     *
     * @return the name of primary key, null if no primary key
     */
    public String getPkColName() {
        return primaryKey;
    }

    /**
     * Check if a column exists, may be faster than getColNames().contains()
     *
     * @param column column name
     * @return exists or not
     */
    public boolean hasColumn(String column) {
        return columns.containsKey(column);
    }

    /**
     * return comma separated list of column names
     */
    public String getColumnNamesString() {
        if (orderedColumnName.size() == 0) return "";
        StringBuilder sb = new StringBuilder(orderedColumnName.get(0));
        for (int i = 1; i < orderedColumnName.size(); i++) {
            sb.append(",");
            sb.append(orderedColumnName.get(i));
        }
        return sb.toString();
    }

    public DataTypeMapping.IDataTypeConverter getTypeConvertorForColumn(String columnName) {
        ObjectFieldModifier fm = columns.get(columnName);
        if (fm == null) throw new IllegalArgumentException("no such column: " + columnName);
        return fm.typeConverter;
    }





    /* ************************ *
     * Object read/modification *
     * ************************ */

    public Object getSqlValue(T obj, String columnName) {
        ObjectFieldModifier fm = columns.get(columnName);
        if (fm == null) throw new IllegalArgumentException("no such column: " + columnName);
        return fm.getSqlObject(obj);
    }

    public void setSqlValue(T obj, String columnName, Object newSqlValue) {
        ObjectFieldModifier fm = columns.get(columnName);
        if (fm == null) throw new IllegalArgumentException("no such column: " + columnName);
        fm.setSqlObject(obj, newSqlValue);

    }

    /**
     * Construct ONE table object from Java ResultSet.
     * Only CURRENT result row will be picked
     */
    public T getObjectFromResultSet(ResultSet rs) throws ReflectiveOperationException, SQLException {
        T obj = ctor.newInstance();
        for (String colName : getColNames()) {
            Object colValue = rs.getObject(colName);
            setSqlValue(obj, colName, colValue);
        }
        return obj;
    }

    /**
     * Get certain columns(fields) from a table object
     * and the column objects should have been converted to database acceptable objects: long/float/string
     *
     * @param obj     the java object
     * @param columns the column names, or null for all columns
     * @return columnName to columnData map
     */
    public Map<String, Object> getColumnObjectMap(T obj, String... columns) {
        List<String> columnList = new ArrayList<>();
        Map<String, Object> objects = new HashMap<>();
        if (columns == null || columns.length == 0) {
            columnList.addAll(orderedColumnName);
        } else {
            columnList.addAll(Arrays.asList(columns));
        }
        for (String colName : columnList) {
            if (!this.columns.containsKey(colName))
                throw new RuntimeException("column " + colName + " not in object " + getJavaClass().getCanonicalName());
            objects.put(colName, this.columns.get(colName).getSqlObject(obj));
        }
        return objects;
    }
}
