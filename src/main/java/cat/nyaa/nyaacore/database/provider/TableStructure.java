package cat.nyaa.nyaacore.database.provider;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

class TableStructure<T> {
    /* class -> TableStructure cache */
    private static final Map<Class<?>, TableStructure<?>> structured_tables = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <X> TableStructure<X> fromClass(Class<X> cls, boolean sqlite) {
        if (structured_tables.containsKey(cls)) return (TableStructure<X>) structured_tables.get(cls);
        TableStructure<X> ts = new TableStructure<>(cls, sqlite);
        structured_tables.put(cls, ts);
        return ts;
    }
    final boolean sqlite;
    final Class<T> tableClass;
    final String tableName;
    final Map<String, ColumnStructure> columns = new HashMap<>();
    final String primaryKey; // null if no primary key
    final List<String> orderedColumnName = new ArrayList<>();

    private TableStructure(Class<T> tableClass, boolean sqlite) {
        Table annoDT = tableClass.getDeclaredAnnotation(Table.class);
        if (annoDT == null)
            throw new IllegalArgumentException("Class missing table annotation: " + tableClass.getName());
        this.sqlite = sqlite;
        this.tableName = annoDT.name();
        this.tableClass = tableClass;
        String primKeyName = null;

        // load all the fields
        for (Field f : tableClass.getDeclaredFields()) {
            Column columnAnnotation = f.getAnnotation(Column.class);
            if (columnAnnotation == null) continue;
            ColumnStructure structure = new ColumnStructure(this, f, columnAnnotation, sqlite);
            if (columns.containsKey(structure.getName()))
                throw new RuntimeException("Duplicated column name: " + structure.getName());
            if (structure.isPrimary()) {
                if (primKeyName != null) throw new RuntimeException("Duplicated primary key at: " + f.getName());
                primKeyName = structure.getName();
            }
            columns.put(structure.getName(), structure);
        }

        // load all the getter/setter
        for (Method m : tableClass.getDeclaredMethods()) {
            Column columnAnnotation = m.getAnnotation(Column.class);
            if (columnAnnotation == null) continue;
            ColumnStructure structure = new ColumnStructure(this, m, columnAnnotation, sqlite);
            if (columns.containsKey(structure.getName()))
                throw new RuntimeException("Duplicated column name: " + structure.getName());
            if (structure.isPrimary()) {
                if (primKeyName != null) throw new RuntimeException("Duplicated primary key at: " + m.getName());
                primKeyName = structure.getName();
            }
            columns.put(structure.getName(), structure);
        }

        primaryKey = primKeyName;
        orderedColumnName.addAll(columns.keySet());
        orderedColumnName.sort(String::compareTo);
    }

    public Class<T> getTableClass() {
        return tableClass;
    }

    public ColumnStructure getColumn(String columnName) {
        return columns.get(columnName);
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public List<String> getOrderedColumnName() {
        return orderedColumnName;
    }

    public String getTableName() {
        return tableName;
    }

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

    public String getCreateTableSQL() {
        StringJoiner colStr = new StringJoiner(",");
        for (String colName : orderedColumnName) {
            colStr.add(columns.get(colName).getTableCreationScheme());
        }
        return String.format("CREATE TABLE IF NOT EXISTS %s(%s)", tableName, colStr.toString());
    }

    /**
     * Get certain columns(fields) from a table object
     * and the column objects should have been converted to database acceptable objects: long/float/string
     *
     * @param obj     the java object
     * @param columns the column names, or null for all columns
     * @return columnName -> columnData map
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
            objects.put(colName, this.columns.get(colName).fetchFromObject(obj));
        }
        return objects;
    }

    /**
     * Construct ONE table object from Java ResultSet.
     * Only CURRENT result row will be picked
     */
    public T getObjectFromResultSet(ResultSet rs) throws ReflectiveOperationException, SQLException {
        T obj = tableClass.newInstance();
        for (String colName : orderedColumnName) {
            Object colValue = rs.getObject(colName);
            this.columns.get(colName).saveToObject(obj, colValue);
        }
        return obj;
    }
}
