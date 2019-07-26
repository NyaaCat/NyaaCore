package cat.nyaa.nyaacore.orm.backends;

import cat.nyaa.nyaacore.orm.DataTypeMapping;
import cat.nyaa.nyaacore.orm.NonUniqueResultException;
import cat.nyaa.nyaacore.orm.WhereClause;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A typed table is a table whose schema is determined by a Java type.
 * Default JDBC-based implementations.
 *
 * @param <T> the table type
 */
abstract class BaseTypedTable<T> implements ITypedTable<T> {

    /**
     * Downstream plugins should *NEVER* use this. YOU'VE BEEN WARNED!
     *
     * @return underlying implementation-specific connection
     */
    protected abstract Connection getConnection();

    @Override
    public void delete(WhereClause where) {
        String sql = "DELETE FROM " + getTableName();
        List<Object> objects = new ArrayList<>();
        sql = where.appendWhereClause(sql, objects, getJavaTypeModifier());
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            stmt.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public void insert(T object) {
        String sql = String.format("INSERT INTO %s(%s) VALUES(?", getTableName(), getJavaTypeModifier().getColumnNamesString());
        for (int i = 1; i < getJavaTypeModifier().getColNames().size(); i++) sql += ",?";
        sql += ")";
        Map<String, Object> objMap = getJavaTypeModifier().getColumnObjectMap(object);
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            for (int i = 1; i <= getJavaTypeModifier().getColNames().size(); i++) {
                String colName = getJavaTypeModifier().getColNames().get(i - 1);
                if (!objMap.containsKey(colName) || objMap.get(colName) == null) {
                    stmt.setNull(i, Types.NULL);
                } else {
                    stmt.setObject(i, objMap.get(colName));
                }
            }
            stmt.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(sql + "\n" + objMap.toString(), ex);
        }
    }

    @Override
    public List<T> select(WhereClause where) {
        String sql = "SELECT " + getJavaTypeModifier().getColumnNamesString() + " FROM " + getTableName();
        List<Object> objects = new ArrayList<>();
        sql = where.appendWhereClause(sql, objects, getJavaTypeModifier());
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            List<T> results = new ArrayList<T>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T obj = getJavaTypeModifier().getObjectFromResultSet(rs);
                    results.add(obj);
                }
            }
            return results;
        } catch (SQLException | ReflectiveOperationException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public T selectUnique(WhereClause where) throws NonUniqueResultException {
        T result = selectUniqueUnchecked(where);
        if (result == null) {
            throw new NonUniqueResultException("SQL Selection has no result or not unique");
        }
        return result;
    }


    @Override
    public T selectUniqueUnchecked(WhereClause where) {
        String sql = "SELECT " + getJavaTypeModifier().getColumnNamesString() + " FROM " + getTableName();
        List<Object> objects = new ArrayList<>();
        sql = where.appendWhereClause(sql, objects, getJavaTypeModifier());
        sql += " LIMIT 2";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            T result = null;
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = getJavaTypeModifier().getObjectFromResultSet(rs);
                    if (rs.next()) result = null; // if more than one results, then return null;
                }
            }
            return result;
        } catch (SQLException | ReflectiveOperationException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public int count(WhereClause where) {
        String sql = "SELECT COUNT(*) AS C FROM " + getTableName();
        List<Object> objects = new ArrayList<>();
        sql = where.appendWhereClause(sql, objects, getJavaTypeModifier());
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("C");
                    return count;
                } else {
                    throw new RuntimeException("COUNT() returns empty result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public void update(T obj, WhereClause where, String... columns) {
        List<String> updatedColumns = new ArrayList<>();
        Map<String, Object> newValues = getJavaTypeModifier().getColumnObjectMap(obj, columns);
        if (columns == null || columns.length <= 0) {
            updatedColumns.addAll(getJavaTypeModifier().getColNames());
        } else {
            updatedColumns.addAll(Arrays.asList(columns));
        }

        List<Object> parameters = new ArrayList<>();
        String sql = "UPDATE " + getTableName() + " SET ";
        for (int i = 0; i < updatedColumns.size(); i++) {
            if (i > 0) sql += ",";
            sql += updatedColumns.get(i) + "=?";
            parameters.add(newValues.get(updatedColumns.get(i)));
        }

        sql = where.appendWhereClause(sql, parameters, getJavaTypeModifier());
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int idx = 1;
            for (Object o : parameters) {
                if (o == null) {
                    stmt.setNull(idx, Types.NULL);
                } else {
                    stmt.setObject(idx, o);
                }
                idx++;
            }
            stmt.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public <R> R selectSingleton(String query, DataTypeMapping.IDataTypeConverter<R> resultTypeConverter) {
        String sql = String.format("SELECT %s FROM %s", query, getTableName());
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.getMetaData().getColumnCount() != 1) {
                throw new RuntimeException("result has multiple columns");
            }
            if (rs.next()) {
                R ret = resultTypeConverter.toJavaType(rs.getObject(1));
                if (rs.next()) {
                    throw new RuntimeException("result has multiple rows");
                }
                return ret;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
