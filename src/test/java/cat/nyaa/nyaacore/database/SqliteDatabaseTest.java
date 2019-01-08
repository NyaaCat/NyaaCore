package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.provider.SQLiteDatabase;
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
    private RelationalDB db2;

    @Before
    public void prepareDatabase() {
        SQLiteDatabase.executor = (p) -> Runnable::run;
        SQLiteDatabase.logger = (p) -> Logger.getLogger("NyaaCoreTest");

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
        db2 = DatabaseUtils.get("sqlite", mockPlugin, conf, RelationalDB.class);
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
    public void testTransInsertNotVisibleBeforeCommit() {
        try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            assertEquals(0, db2.query(TestTable.class).count());
            assertEquals(0, db.query(TestTable.class).count());
            assertEquals(2, query.count());
            query.commit();
        }
        try (Query<TestTable> query2 = db2.queryTransactional(TestTable.class)) {
            query2.insert(new TestTable(3L, "test2", UUID.randomUUID(), UUID.randomUUID()));
            query2.insert(new TestTable(4L, "test2", UUID.randomUUID(), UUID.randomUUID()));
            assertEquals(2, db2.query(TestTable.class).count());
            assertEquals(2, db.query(TestTable.class).count());
            assertEquals(4, query2.count());
            query2.commit();
        }
        try (Query<TestTable> query3 = db2.queryTransactional(TestTable.class)) {
            query3.insert(new TestTable(5L, "test3", UUID.randomUUID(), UUID.randomUUID()));
            query3.insert(new TestTable(6L, "test3", UUID.randomUUID(), UUID.randomUUID()));
            assertEquals(2, query3.where("string", "=", "test3").count());
            assertEquals(0, db.query(TestTable.class).where("string", "=", "test3").count());
            assertEquals(4, db2.query(TestTable.class).count());
            assertEquals(4, db.query(TestTable.class).count());
            assertEquals(6, query3.reset().count());
            throwException();
            query3.commit();
        } catch (Exception ignored) {
        }
        assertEquals(4, db.query(TestTable.class).count());
        assertEquals(4, db2.query(TestTable.class).count());
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

    @After
    public void closeDatabase() {
        db.close();
        db2.close();
        if (!new File("./testdb.db").delete()) {
            throw new IllegalStateException();
        }
    }
}
