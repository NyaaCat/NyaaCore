package cat.nyaa.nyaacore.database.relational;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TableStructure<T> {
    /* class -> TableStructure cache */
    private static final Map<Class<?>, TableStructure<?>> structured_tables = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <X> TableStructure<X> fromClass(Class<X> cls) {
        if (structured_tables.containsKey(cls)) return (TableStructure<X>) structured_tables.get(cls);
        TableStructure<X> ts = new TableStructure<>(cls);
        structured_tables.put(cls, ts);
        return ts;
    }

    public final Class<T> tableClass;
    public final String tableName;

    public final Map<String, ColumnStructure> columns = new HashMap<>();
    public final String primaryKey; // null if no primary key
    public final List<String> orderedColumnName = new ArrayList<>();

    private TableStructure(Class<T> tableClass) {
        Table annoDT = tableClass.getDeclaredAnnotation(Table.class);
        if (annoDT == null)
            throw new IllegalArgumentException("Class missing table annotation: " + tableClass.getName());

        this.tableClass = tableClass;
        if (annoDT.name().isEmpty()) {
            this.tableName = tableClass.getSimpleName();
        } else {
            this.tableName = annoDT.name();
        }

        String primKeyName = null;

        // load all the fields
        for (Field f : tableClass.getDeclaredFields()) {
            Column columnAnnotation = f.getAnnotation(Column.class);
            if (columnAnnotation == null) continue;
            ColumnStructure structure = new ColumnStructure(this, f, columnAnnotation);
            if (columns.containsKey(structure.getName()))
                throw new RuntimeException("Duplicated column name: " + structure.getName());
            if (structure.primary) {
                if (primKeyName != null) throw new RuntimeException("Duplicated primary key at: " + f.getName());
                primKeyName = structure.getName();
            }
            columns.put(structure.getName(), structure);
        }

        // load all the getter/setter
        for (Method m : tableClass.getDeclaredMethods()) {
            Column columnAnnotation = m.getAnnotation(Column.class);
            if (columnAnnotation == null) continue;
            ColumnStructure structure = new ColumnStructure(this, m, columnAnnotation);
            if (columns.containsKey(structure.getName()))
                throw new RuntimeException("Duplicated column name: " + structure.getName());
            if (structure.primary) {
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
        if (primaryKey != null) {
            colStr.add(String.format("CONSTRAINT constraint_PK PRIMARY KEY (%s)", primaryKey));
        }
        return String.format("CREATE TABLE IF NOT EXISTS %s(%s)", tableName, colStr.toString());
    }

    /**
     * @deprecated magic string
     */
    @Deprecated
    public String getCreateTableSQL(String dialect) {
        if (dialect.equalsIgnoreCase("sqlite")) {
            StringJoiner colStr = new StringJoiner(",");
            for (String colName : orderedColumnName) {
                ColumnStructure ct = columns.get(colName);
                if (ct.primary) {
                    if (ct.sqlType == DataTypeMapping.Types.INTEGER || ct.sqlType == DataTypeMapping.Types.BIGINT) {
                        colStr.add(ct.getName() + " INTEGER PRIMARY KEY");
                    } else {
                        colStr.add(ct.getTableCreationScheme() + " PRIMARY KEY");
                    }
                } else {
                    colStr.add(columns.get(colName).getTableCreationScheme());
                }
            }
            return String.format("CREATE TABLE IF NOT EXISTS %s(%s)", tableName, colStr.toString());
        } else if (dialect.equalsIgnoreCase("mysql")) {
            return getCreateTableSQL();
        } else {
            throw new IllegalArgumentException("unknown dialect");
        }
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
            objects.put(colName, this.columns.get(colName).getSqlObject(obj));
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
            this.columns.get(colName).setSqlObject(obj, colValue);
        }
        return obj;
    }
}
