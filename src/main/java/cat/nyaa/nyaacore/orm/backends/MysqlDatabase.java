package cat.nyaa.nyaacore.orm.backends;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlDatabase implements IConnectedDatabase {

    public MysqlDatabase(Connection conn) {

    }

    @Override
    public <T> ITable<T> getTable(Class<T> recordClass) {
        return null;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean verifySchema(String tableName, Class recordClass) {
        return false;
    }

//    public String getCreateTableSQL() {
//        StringJoiner colStr = new StringJoiner(",");
//        for (String colName : orderedColumnName) {
//            colStr.add(columns.get(colName).getTableCreationScheme());
//        }
//        if (primKeyStructure != null) {
//            if (primKeyStructure.sqlType.isBlobOrText() && primKeyStructure.getLength() > 0) {
//                colStr.add(String.format("CONSTRAINT constraint_PK PRIMARY KEY (%s(%d))", primaryKey, primKeyStructure.getLength()));
//            } else {
//                colStr.add(String.format("CONSTRAINT constraint_PK PRIMARY KEY (%s)", primaryKey));
//            }
//        }
//        return String.format("CREATE TABLE IF NOT EXISTS %s(%s)", tableName, colStr.toString());
//    }
//    public void createTable(Class<?> cls) {
//        Validate.notNull(cls);
//        if (createdTableClasses.contains(cls)) return;
//        try {
//            if (!mainConnLock.tryAcquire(10, TimeUnit.SECONDS)) {
//                throw new IllegalStateException();
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            createTable(cls, getConnection());
//        } finally {
//            mainConnLock.release();
//        }
//    }
//
//    @Override
//    public void createTable(Class<?> cls) {
//        Validate.notNull(cls);
//        if (createdTableClasses.contains(cls)) return;
//        ObjectModifier ts = ObjectModifier.fromClass(cls);
//        String sql = ts.getCreateTableSQL();
//        try (Statement smt = getConnection().createStatement()) {
//            smt.executeUpdate(sql);
//            createdTableClasses.add(cls);
//        } catch (SQLException ex) {
//            throw new RuntimeException(sql, ex);
//        }
//    }
}
