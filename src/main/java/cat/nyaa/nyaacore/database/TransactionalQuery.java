package cat.nyaa.nyaacore.database;

import java.sql.SQLException;

public interface TransactionalQuery<T> extends Query<T> {
    void rollback();
    void commit();
}
