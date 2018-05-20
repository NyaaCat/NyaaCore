package cat.nyaa.nyaacore.database;

public interface TransactionalQuery<T> extends Query<T> {

    @Override
    TransactionalQuery<T> reset();

    @Override
    TransactionalQuery<T> whereEq(String columnName, Object obj);

    @Override
    TransactionalQuery<T> where(String columnName, String comparator, Object obj);

    void rollback();

    void commit();
}
