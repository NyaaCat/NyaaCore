package cat.nyaa.nyaacore.database;

import cat.nyaa.nyaacore.database.keyvalue.KeyValueDB;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class MapProviderTest {
    @Test
    public void test1() {
        KeyValueDB<Integer, String> db = (KeyValueDB) DatabaseUtils.get("map", null, null, KeyValueDB.class);
        db.connect();
        Assert.assertNull(db.get(1));
        db.close();
    }

    @Test
    public void test2() {
        KeyValueDB<String, String> db = (KeyValueDB) DatabaseUtils.get("map", null, null, KeyValueDB.class);
        db.connect();
        db.put("s", "t");
        db.put("k", "v");
        Assert.assertEquals("v", db.get("k"));
        db.close();
    }

    @Test
    public void test3() {
        try (KeyValueDB<String, String> db = (KeyValueDB) DatabaseUtils.get("map", null, null, KeyValueDB.class).connect()) {
            db.put("k", "v");
            db.asMap().clear();
            Assert.assertNull(db.get("k"));
            db.put("k", "v0");
            Assert.assertTrue(db.getAll("k").contains("v0"));
        }
    }
}
