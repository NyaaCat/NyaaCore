package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.keyvalue.KeyValueDB;
import cat.nyaa.nyaacore.database.provider.MapProvider;
import org.junit.Assert;
import org.junit.Test;

public class MapProviderTest {
    @Test
    public void test1() {
        try ( KeyValueDB<Integer, String> db = new MapProvider.MapDB<>().connect()) {
            Assert.assertNull(db.get(1));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test2() {
        try ( KeyValueDB<String, String> db = new MapProvider.MapDB<>().connect()) {
            db.put("s", "t");
            db.put("k", "v");
            Assert.assertEquals("v", db.get("k"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test3() {
        try ( KeyValueDB<String, String> db = new MapProvider.MapDB<>().connect()) {
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
