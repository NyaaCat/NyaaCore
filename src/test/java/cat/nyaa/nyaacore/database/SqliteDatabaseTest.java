package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.orm.backends.SQLiteDatabase;
import cat.nyaa.nyaacore.database.relational.Query;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import org.bukkit.plugin.Plugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SqliteDatabaseTest {
    private RelationalDB db;

    @Before
    public void prepareDatabase() {
        SQLiteDatabase.executorSupplier = (p) -> Runnable::run;
        SQLiteDatabase.loggerSupplier = (p) -> Logger.getLogger("NyaaCoreTest");

        Map<String, Object> conf = new HashMap<>();
        File file = new File("./testdb.db");
        if (file.exists()) {
            if (!new File("./testdb.db").delete()) {
                throw new IllegalStateException();
            }
        }
        conf.put("file", "testdb.db");
        conf.put("autoscan", "false");
        conf.put("tables", Collections.singleton(TestTable.class.getName()));
        Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(new File("./"));
        when(mockPlugin.getLogger()).thenReturn(Logger.getGlobal());
        db = DatabaseUtils.get("sqlite", mockPlugin, conf, RelationalDB.class);
    }

    @Test
    public void testClear() {
        db.query(TestTable.class).reset();
    }

    @Test
    public void testInsert() {
        db.query(TestTable.class).insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
        db.query(TestTable.class).insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(2, db.query(TestTable.class).count());
    }

    @Test
    public void testTransInsert() throws Exception {
        try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.commit();
        }
        assertEquals(2, db.query(TestTable.class).count());
    }

    @Test
    public void testTransInsertRollbackedByNonSqlEx() {
        db.query(TestTable.class).insert(new TestTable(3L, "test", UUID.randomUUID(), UUID.randomUUID()));
        db.query(TestTable.class).insert(new TestTable(4L, "test", UUID.randomUUID(), UUID.randomUUID()));
        try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            throwException();
            fail();
        } catch (Exception ignored) {
        }
        assertEquals(2, db.query(TestTable.class).count());
    }

    private static void throwException() {
        throw new RuntimeException();
    }

    @Test
    public void testTransInsertRollbackedBySqlEx() {
        db.query(TestTable.class).insert(new TestTable(3L, "test", UUID.randomUUID(), UUID.randomUUID()));
        db.query(TestTable.class).insert(new TestTable(4L, "test", UUID.randomUUID(), UUID.randomUUID()));
        try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            //Throws SQLException because of comparator
            query.where("string", " throw ", "test").count();
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof SQLException);
        }
        assertEquals(2, db.query(TestTable.class).count());
    }
    
    @Test
    public void testTransUpdateParallel() {
        assertEquals(0, db.query(TestTable.class).count());
        db.query(TestTable.class).insert(new TestTable(1L, "0", UUID.randomUUID(), UUID.randomUUID()));
        IntStream.range(0, 100).parallel().forEach((i) -> {
            try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
                TestTable current = query.whereEq("id", 1L).selectUniqueForUpdate();
                assertNotNull(current);
                int currentInt = Integer.parseInt(current.string);
                current.string = String.valueOf(currentInt + 1);
                query.update(current, "string");
                query.commit();
            }
        });
        assertEquals("100", db.query(TestTable.class).selectUnique().string);
    }

    /**
     * SQLite database transactions are serializable
     */
    @Test
    public void testTransUpdateParallelNotForUpdate() {
        assertEquals(0, db.query(TestTable.class).count());
        db.query(TestTable.class).insert(new TestTable(1L, "0", UUID.randomUUID(), UUID.randomUUID()));
        IntStream.range(0, 100).parallel().forEach((i) -> {
            try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
                TestTable current = query.whereEq("id", 1L).selectUnique();
                assertNotNull(current);
                int currentInt = Integer.parseInt(current.string);
                current.string = String.valueOf(currentInt + 1);
                query.update(current, "string");
                query.commit();
            }
        });
        assertEquals("100", db.query(TestTable.class).selectUnique().string);
    }

    /**
     * Leaked SQLiteQuery should will be picked up by FinalizableReferenceQueue and rollbacked
     */
    @Test
    public void testTransUnclosed() {
        assertEquals(0, db.query(TestTable.class).count());
        db.query(TestTable.class).insert(new TestTable(1L, "0", UUID.randomUUID(), UUID.randomUUID()));
        leakQuery();
        IntStream.range(0, 100).parallel().forEach((i) -> {
            System.gc();
            try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
                TestTable current = query.whereEq("id", 1L).selectUnique();
                assertNotNull(current);
                int currentInt = Integer.parseInt(current.string);
                current.string = String.valueOf(currentInt + 1);
                query.update(current, "string");
                query.commit();
            }
        });
        assertEquals("100", db.query(TestTable.class).selectUnique().string);
    }

    private void leakQuery() {
        Query<TestTable> query = db.queryTransactional(TestTable.class);
        TestTable current = query.whereEq("id", 1L).selectUnique();
        assertNotNull(current);
        int currentInt = Integer.parseInt(current.string);
        current.string = String.valueOf(currentInt + 1);
        query.update(current, "string");
    }

    @After
    public void closeDatabase() {
        db.close();
        if (!new File("./testdb.db").delete()) {
            throw new IllegalStateException();
        }
    }
}
