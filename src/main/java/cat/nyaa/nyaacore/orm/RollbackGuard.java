package cat.nyaa.nyaacore.orm;

import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This provides a easy way to rollback database in case of Java program exceptions.
 * You are not expected to use this for real transactions.
 * <p>
 * Here is the use case:
 * <pre>
 *     try (RollbackGuard guard = new RollbackGuard(db)) {
 * 	        // something could throw exception
 * 	        guard.commit();
 *     }
 * </pre>
 * <p>
 * If exception is thrown and guard is not committed, the guard will automatically rollback the connection.
 */
public class RollbackGuard implements AutoCloseable {
    private final Connection conn;
    private boolean needRollbackOnClose = false;

    public RollbackGuard(IConnectedDatabase db) {
        conn = db.getConnection();
        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create ConnectedDatabaseTransactionGuard", ex);
        }
        needRollbackOnClose = true;
    }

    public void commit() {
        try {
            conn.commit();
            conn.setAutoCommit(true);
            needRollbackOnClose = false;
        } catch (SQLException ex) {
            needRollbackOnClose = true;
        }
    }

    @Override
    public void close() throws Exception {
        if (needRollbackOnClose) conn.rollback();
        conn.setAutoCommit(true);
    }
}
