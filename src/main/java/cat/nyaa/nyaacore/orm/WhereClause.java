package cat.nyaa.nyaacore.orm;

import java.util.ArrayList;
import java.util.List;

public class WhereClause {
    public static final WhereClause EMPTY = new WhereClause();

    private final List<String> columns = new ArrayList<>();
    private final List<String> comparators = new ArrayList<>();
    private final List<Object> javaObjects = new ArrayList<>();

    public WhereClause() {
    }

    public WhereClause(String columnName, String comparator, Object obj) {
        this.where(columnName, comparator, obj);
    }

    public static WhereClause EQ(String columnName, Object obj) {
        return new WhereClause().whereEq(columnName, obj);
    }

    public WhereClause whereEq(String columnName, Object obj) {
        if (obj == null) throw new IllegalArgumentException("please use `IS' to compare NULL value");
        return where(columnName, "=", obj);
    }

    /**
     * comparator can be any SQL comparator.
     * e.g. =, &gt;, &lt;
     *
     * @param columnName
     * @param comparator if it's a keyword, you need to add spaces before and after, e.g. " LIKE ", if obj is null, this should use " IS " instead of "="
     * @param obj        the java object, can be null
     */
    public WhereClause where(String columnName, String comparator, Object obj) {
        if (columnName == null || comparator == null) throw new IllegalArgumentException();
        columns.add(columnName);
        comparators.add(comparator);
        javaObjects.add(obj);
        return this;
    }

    /**
     * sql in e.g.  "DELETE FROM table"
     * sql out e.g. "DELETE FROM table WHERE col1=? AND col2=? AND col3=?"
     *
     * @param sql
     * @param positionalParameterHolder
     * @param columnTypeMapping
     * @return
     */
    public String appendWhereClause(String sql, List<Object> positionalParameterHolder, ObjectModifier columnTypeMapping) {
        if (columns.size() > 0) {
            sql += " WHERE";
            for (int idx = 0; idx < columns.size(); idx++) {
                if (idx > 0) sql += " AND";
                sql += " " + columns.get(idx) + comparators.get(idx) + "?";
                positionalParameterHolder.add(columnTypeMapping.getTypeConvertorForColumn(columns.get(idx)).toSqlType(javaObjects.get(idx)));
            }
        }
        return sql;
    }
}
