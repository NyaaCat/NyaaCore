package cat.nyaa.nyaacore.database;

import org.bukkit.plugin.Plugin;
import org.junit.*;
import org.sqlite.SQLiteException;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SqliteDatabaseTest {
    private RelationalDB db;
    private RelationalDB db2;
    @Before
    public void prepareDatabase() {
        Map<String, Object> conf = new HashMap<>();
        File file = new File("./testdb.db");
        if(file.exists()){
            if(!new File("./testdb.db").delete()){
                throw new IllegalStateException();
            }
        }
        conf.put("file", "testdb.db");
        conf.put("autoscan", "false");
        conf.put("tables", Collections.singleton(TestTable.class.getName()));
        Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(new File("./"));
        when(mockPlugin.getLogger()).thenReturn(Logger.getGlobal());
        db = DatabaseUtils.get("sqlite", mockPlugin, conf).connect();
        db2 = DatabaseUtils.get("sqlite", mockPlugin, conf).connect();
    }

    @Test
    public void testClear(){
        db.query(TestTable.class).reset();
    }

    @Test
    public void testInsert(){
        db.query(TestTable.class).insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
        db.query(TestTable.class).insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(2, db.query(TestTable.class).count());
    }

    @Test
    public void testTransInsert(){
        try(TransactionalQuery<TestTable> query = db.transaction(TestTable.class)){
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
        }
        assertEquals(2, db.query(TestTable.class).count());
    }

    @Test
    public void testTransInsertRollbackedByNonSqlEx(){
        try(TransactionalQuery<TestTable> query = db.transaction(TestTable.class, true)){
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            db.auto(TestTable.class).insert(new TestTable(3L, "test", UUID.randomUUID(), UUID.randomUUID()));
            db.query(TestTable.class).insert(new TestTable(4L, "test", UUID.randomUUID(), UUID.randomUUID()));
            throwException();
            query.commit();
        } catch (Exception ignored){
        }
        assertEquals(0, db.query(TestTable.class).count());
    }

    private static void throwException() {
        throw new RuntimeException();
    }

    @Test
    public void testTransInsertRollbackedBySqlEx(){
        try(TransactionalQuery<TestTable> query = db.transaction(TestTable.class)){
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            db.auto(TestTable.class).insert(new TestTable(3L, "test", UUID.randomUUID(), UUID.randomUUID()));
            db.auto(TestTable.class).insert(new TestTable(4L, "test", UUID.randomUUID(), UUID.randomUUID()));
            //Throws SQLiteException because of comparator
            query.where("string", " throw ", "test").count();
        } catch (Exception e){
            assertEquals(e.getCause().getClass(), SQLiteException.class);
        }
        assertEquals(0, db.query(TestTable.class).count());
    }

    @Test
    public void testTransInsertNotVisibleBeforeCommit(){
        try(TransactionalQuery<TestTable> query = db.transaction(TestTable.class)){
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            assertEquals(0, db2.auto(TestTable.class).count());
            assertEquals(2, db.auto(TestTable.class).count());
            assertEquals(2, query.count());
        }
        try(TransactionalQuery<TestTable> query2 = db2.transaction(TestTable.class)){
            query2.insert(new TestTable(3L, "test2", UUID.randomUUID(), UUID.randomUUID()));
            query2.insert(new TestTable(4L, "test2", UUID.randomUUID(), UUID.randomUUID()));
            assertEquals(4, db2.auto(TestTable.class).count());
            assertEquals(2, db.auto(TestTable.class).count());
            assertEquals(4, query2.count());
        }
        try(TransactionalQuery<TestTable> query3 = db2.transaction(TestTable.class, true)){
            query3.insert(new TestTable(5L, "test3", UUID.randomUUID(), UUID.randomUUID()));
            query3.insert(new TestTable(6L, "test3", UUID.randomUUID(), UUID.randomUUID()));
            assertEquals(2, query3.where("string", "=", "test3").count());
            assertEquals(0, db.auto(TestTable.class).where("string", "=", "test3").count());
            assertEquals(6, db2.auto(TestTable.class).count());
            assertEquals(4, db.auto(TestTable.class).count());
            assertEquals(6, query3.reset().count());
            throwException();
            query3.commit();
        } catch (Exception ignored){}
        assertEquals(4, db.auto(TestTable.class).count());
        assertEquals(4, db2.auto(TestTable.class).count());
    }

    @After
    public void closeDatabase() {
        db.close();
        db2.close();
        if(!new File("./testdb.db").delete()){
            throw new IllegalStateException();
        }
    }
}
