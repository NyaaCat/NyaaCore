package cat.nyaa.nyaacore.orm.backends;

import cat.nyaa.nyaacore.orm.NonUniqueResultException;
import cat.nyaa.nyaacore.orm.ObjectModifier;
import cat.nyaa.nyaacore.orm.WhereClause;

import java.util.List;

/**
 * A typed table is a table whose schema is determined by a Java type.
 *
 * @param <T> the table type
 */
public interface ITypedTable<T> extends ITable {

    /**
     * @return name of the table
     */
    String getTableName();

    /**
     * @return Java object modifier for the type of this table
     */
    ObjectModifier<T> getJavaTypeModifier();

    /**
     * the where clauses are ignored
     */
    void insert(T newRecord);

    /**
     * SELECT * FROM this_table WHERE ...
     *
     * @return all select rows
     */
    List<T> select(WhereClause where);

    /**
     * remove records matching the where clauses
     */
    void delete(WhereClause where);

    /**
     * Update record according to the where clauses
     *
     * @param newRecord new values for columns
     * @param columns   columns need to be updated, update all columns if empty
     */
    void update(T newRecord, WhereClause where, String... columns);

    /**
     * Select only one record.
     *
     * @return the record, or throw exception if not unique
     */
    T selectUnique(WhereClause where) throws NonUniqueResultException;

    /**
     * Select only one record.
     * <p>
     * This method is called "unchecked" because it does not throw
     * checked exception {@link NonUniqueResultException}
     * <p>
     * Instead, it returns null if not unique
     *
     * @param where
     * @return implNote: has small performance advantage over {@link ITypedTable#select(WhereClause)} because it does not convert all records to java objects
     */
    T selectUniqueUnchecked(WhereClause where);

    /**
     * A short hand for select().size();
     *
     * @return number of records to be selected.
     * implNote: has small performance advantage over {@link ITypedTable#select(WhereClause)}.size() because it "SELECT COUNT(*)"
     */
    int count(WhereClause where);
}
