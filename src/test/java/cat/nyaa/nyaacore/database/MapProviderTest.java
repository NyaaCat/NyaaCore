package cat.nyaa.nyaacore.database;

import org.junit.Assert;
import org.junit.Test;

public class MapProviderTest {
    @Test
    public void test1() {
        KeyValueDB<Integer, String> db = DatabaseUtils.get(null, "map", null);
        db.connect();
        Assert.assertNull(db.get(1));
    }

    @Test
    public void test2() {
        KeyValueDB<String, String> db = DatabaseUtils.get(null,  "map", null);
        db.connect();
        db.put("s", "t");
        db.put("k", "v");
        Assert.assertEquals("v", db.get("k"));
    }

    @Test
    public void test3() {
        KeyValueDB<String, String> db = DatabaseUtils.get(null, "map", null);
        db.connect();
        db.put("k", "v");
        db.asMap().clear();
        Assert.assertNull(db.get("k"));
        db.put("k", "v0");
        Assert.assertTrue(db.getAll("k").contains("v0"));
    }
}
