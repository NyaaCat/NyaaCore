package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.provider.MysqlDatabase;
import cat.nyaa.nyaacore.database.relational.Query;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.bukkit.plugin.Plugin;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MysqlDatabaseTest {
    private static int port;
    private static DB embeddedDB;
    private RelationalDB db;
    private RelationalDB db2;

    @BeforeClass
    public static void prepareEmbeddedDB() throws ManagedProcessException, IOException {
        MysqlDatabase.executor = (p) -> Runnable::run;
        MysqlDatabase.logger = (p) -> Logger.getLogger("NyaaCoreTest");
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        String tmpDir = new File(System.getProperty("java.io.tmpdir")).getCanonicalPath();
        configBuilder.setBaseDir(tmpDir + File.separatorChar + "MariaDB4j" + File.separatorChar + "base");
        configBuilder.setLibDir(tmpDir + File.separatorChar + "MariaDB4j" + File.separatorChar + "base" + File.separatorChar + "libs");
        configBuilder.setDataDir(tmpDir + File.separatorChar + "MariaDB4j" + File.separatorChar + "data");
        ServerSocket s = new ServerSocket(0);
        port = s.getLocalPort();
        configBuilder.setPort(port);
        embeddedDB = DB.newEmbeddedDB(configBuilder.build());
        s.close();
        embeddedDB.start();
        port = embeddedDB.getConfiguration().getPort();
    }


    @AfterClass
    public static void shutdownEmbeddedDB() {
        try {
            embeddedDB.stop();
        } catch (Exception e) {
            Logger.getLogger("NyaaCoreTest").log(Level.WARNING, "failed to stop embeddedDB", e);
        }
    }

    @Before
    public void prepareDatabase() throws ManagedProcessException {
        embeddedDB.run("drop database if exists test;");
        embeddedDB.run("create database test;");
        Map<String, Object> conf = new HashMap<>();
        conf.put("url", String.format("jdbc:mysql://127.0.0.1:%d/test?useSSL=false", port));
        conf.put("autoscan", "false");
        conf.put("username", "root");
        conf.put("password", "");
        conf.put("tables", Collections.singleton(TestTable.class.getName()));
        Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(new File("./"));
        when(mockPlugin.getLogger()).thenReturn(Logger.getGlobal());
        db = DatabaseUtils.get("mysql", mockPlugin, conf, RelationalDB.class);
        db2 = DatabaseUtils.get("mysql", mockPlugin, conf, RelationalDB.class);
    }

    @Test
    public void testReset() {
        db.query(TestTable.class).reset();
    }

    @Test
    public void testInsert() {
        db.query(TestTable.class).insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
        db.query(TestTable.class).insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(2, db.query(TestTable.class).count());
    }

    @Test
    public void testTransInsert() {
        assertEquals(0, db.query(TestTable.class).count());
        try (Query<TestTable> query = db.queryTransactional(TestTable.class)) {
            query.insert(new TestTable(1L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.insert(new TestTable(2L, "test", UUID.randomUUID(), UUID.randomUUID()));
            query.commit();
        }
        assertEquals(2, db.query(TestTable.class).count());
    }

    @Test
    public void testTransInsertRollbackedByNonSqlEx() {
        assertEquals(0, db.query(TestTable.class).count());
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
        assertEquals(0, db.query(TestTable.class).count());
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
        assertEquals(0, db.query(TestTable.class).count());
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
        System.out.println(db.query(TestTable.class).selectUnique().string);
        assertTrue(Integer.parseInt(db.query(TestTable.class).selectUnique().string) <= 100);
    }

    @After
    public void closeDatabase() {
        db.close();
        db2.close();
    }
}
