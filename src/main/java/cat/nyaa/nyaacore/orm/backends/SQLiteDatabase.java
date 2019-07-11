package cat.nyaa.nyaacore.orm.backends;

import cat.nyaa.nyaacore.orm.ObjectModifier;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDatabase implements IDatabase {

    private final Connection dbConn;

    public SQLiteDatabase(Connection sqlConnection) {
        if (sqlConnection == null) throw new IllegalArgumentException();
        dbConn = sqlConnection;
    }

    @Override
    public void close() throws SQLException {
        dbConn.close();
    }

    @Override
    public <T> ITable<T> getTable(Class<T> recordClass) {
        return null;
        // TODO
//        if (database has table) {
//            if (table schema matches) {
//                return
//            } else {
//                throw error or perform upgrade
//            }
//        } else {
//            create table
//            return ITable
//        }
    }


    public class SQLiteTypedTable<T> extends BaseTypedTable<T> {
        @Override
        public String getTableName() {
            return null; // TODO
        }

        @Override
        public ObjectModifier<T> getJavaTypeModifier() {
            return null; // TODO
        }

        @Override
        protected Connection getConnection() {
            return dbConn;
        }
    }

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
//    private void createTable(Class<?> cls, Connection conn) {
//        Validate.notNull(cls);
//        if (createdTableClasses.contains(cls)) return;
//        ObjectModifier ts = ObjectModifier.fromClass(cls);
//        String sql = ts.getCreateTableSQL("sqlite");
//        try (Statement smt = conn.createStatement()) {
//            smt.executeUpdate(sql);
//            createdTableClasses.add(cls);
//        } catch (SQLException ex) {
//            throw new RuntimeException(sql, ex);
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
