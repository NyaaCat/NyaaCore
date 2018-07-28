package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.keyvalue.KeyValueDB;
import cat.nyaa.nyaacore.database.provider.MapDatabase;
import org.junit.Assert;
import org.junit.Test;

public class MapProviderTest {
    @Test
    public void test1() {
        try ( KeyValueDB<Integer, String> db = new MapDatabase<>()) {
            Assert.assertNull(db.get(1));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test2() {
        try ( KeyValueDB<String, String> db = new MapDatabase<>()) {
            db.put("s", "t");
            db.put("k", "v");
            Assert.assertEquals("v", db.get("k"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test3() {
        try ( KeyValueDB<String, String> db = new MapDatabase<>()) {
            db.put("k", "v");
            db.asMap().clear();
            Assert.assertNull(db.get("k"));
            db.put("k", "v0");
            Assert.assertTrue(db.getAll("k").contains("v0"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
