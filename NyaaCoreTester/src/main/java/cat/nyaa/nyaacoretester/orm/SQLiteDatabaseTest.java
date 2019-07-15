package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.BackendConfig;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.ITypedTable;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.junit.*;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SQLiteDatabaseTest {

    private static IConnectedDatabase db;
    private ITypedTable<TableTest1> tableTest1;

    @BeforeClass
    public static void connectDatabase() throws SQLException, ClassNotFoundException {
        new File(NyaaCoreTester.instance.getDataFolder(), "testdb.db").delete();
        db = DatabaseUtils.connect(NyaaCoreTester.instance, BackendConfig.sqliteBackend("testdb.db"));
    }

    @Before
    public void openTables() {
        tableTest1 = db.getTable(TableTest1.class);
    }

    @Test
    public void testClear() {
        tableTest1.delete(WhereClause.EMPTY);
    }

    @Test
    public void testInsert() {
        tableTest1.insert(new TableTest1(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
        tableTest1.insert(new TableTest1(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(2, tableTest1.count(WhereClause.EMPTY));
    }

    @Test
    public void testSelect() {
        tableTest1.delete(WhereClause.EMPTY);
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        TableTest1 record = new TableTest1(42L, "test", uuid1, uuid2);
        tableTest1.insert(record);

        List<TableTest1> ret = tableTest1.select(WhereClause.EMPTY);
        assertEquals(1, ret.size());
        assertEquals(record, ret.get(0));
    }

    @After
    public void closeTables() {
        tableTest1 = null;
    }

    @AfterClass
    public static void closeDatabase() throws SQLException {
        db.close();
    }
}
