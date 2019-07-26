package cat.nyaa.nyaacore.orm.backends;

import cat.nyaa.nyaacore.orm.DataTypeMapping;

/**
 * Some low-level APIs to interact with data tables.
 */
public interface ITable {
    default <T> T selectSingleton(String query, DataTypeMapping.IDataTypeConverter<T> resultTypeConverter) {
        throw new UnsupportedOperationException();
    }
}
