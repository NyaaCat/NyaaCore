package cat.nyaa.nyaacore.orm;

import org.bukkit.plugin.Plugin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BundledSQLUtils {
    /**
     * build a statement using provided parameters
     *
     * @param sql            SQL string
     * @param replacementMap {{key}} in the file will be replaced by value. Ignored if null. NOTE: sql injection will happen
     * @param parameters     JDBC's positional parametrized query. Java types.
     * @return statement
     */
    private static PreparedStatement buildStatement(Connection conn, String sql, Map<String, String> replacementMap, Object... parameters) {
        if (replacementMap != null) {
            for (String key : replacementMap.keySet()) {
                sql = sql.replace("{{" + key + "}}", replacementMap.get(key));
            }
        }
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                Object javaObj = parameters[i];
                stmt.setObject(i + 1, DataTypeMapping.getDataTypeConverter(javaObj.getClass()).toSqlType(javaObj));
            }
            return stmt;
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    /**
     * Convert a result set to a list of java objects
     *
     * @param rs  the result set
     * @param cls record type
     * @param <T> record type, do not have to be registered in getTables()
     * @return java object list
     */
    public static <T> List<T> parseResultSet(ResultSet rs, Class<T> cls) {
        try {
            if (rs == null) return new ArrayList<>();
            ObjectModifier<T> table = ObjectModifier.fromClass(cls);
            List<T> results = new ArrayList<T>();
            while (rs.next()) {
                T obj = table.getObjectFromResultSet(rs);
                results.add(obj);
            }
            return results;
        } catch (SQLException | ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Execute a SQL file bundled with the plugin
     *
     * @param plugin         java plugin object, for resource access
     * @param conn           connected database
     * @param filename       full file name, including extension, in resources/sql folder
     * @param replacementMap {{key}} in the file will be replaced by value. Ignored if null. NOTE: sql injection will happen
     * @param cls            class of desired object
     * @param parameters     JDBC's positional parametrized query. Java type.
     * @param <T>            Type of record object
     * @return the result set, null if cls is null.
     */
    public static <T> List<T> queryBundledAs(Plugin plugin, Connection conn, String filename, Map<String, String> replacementMap, Class<T> cls, Object... parameters) {
        String sql;
        try (
                InputStream inputStream = plugin.getResource("sql/" + filename);
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream()
        ) {
            int result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            sql = buf.toString(StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try (PreparedStatement stat = buildStatement(conn, sql, replacementMap, parameters)) {
            boolean hasResult = stat.execute();
            if (cls == null) {
                return null;
            } else if (hasResult) {
                return parseResultSet(stat.getResultSet(), cls);
            } else {
                return new ArrayList<>();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void queryBundled(Plugin plugin, Connection conn, String filename, Map<String, String> replacementMap, Object... parameters) {
        queryBundledAs(plugin, conn, filename, replacementMap, null, parameters);
    }
}
