package cat.nyaa.nyaacore.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ISerializableTest {
    @Test
    public void test1() throws Exception {
        YamlConfiguration cfg = new YamlConfiguration();
        DataObject obj1 = new DataObject();
        DataObject obj2 = new DataObject();
        obj1.fillNestedList();
        obj1.serialize(cfg);
        String yml = cfg.saveToString();
        final String ans = "str: The quick brown fox jumps over the lazy dog\n__class__: cat.nyaa.nyaacore.configuration.DataObject\nnumber_int: 42\nnumber_long: 42\nenum_1: VALUE_A\nenum_2: LEAVES\nmap:\n  key1: value1\n  key2: value2\n  key3: value3\n  key4: value4\nanotherDataObject:\n  x: 42\n  __class__: cat.nyaa.nyaacore.configuration.DataObject$NestedObject\n  d: 42.42\nmapOfDataObject:\n  obj2:\n    x: 42\n    __class__: cat.nyaa.nyaacore.configuration.DataObject$NestedObject\n    d: 42.42\n  obj1:\n    x: 42\n    __class__: cat.nyaa.nyaacore.configuration.DataObject$NestedObject\n    d: 42.42\nnestedList:\n  '0':\n  - 0\n  - 1\n  - 2\n  - 3\n  - 4\n  - 5\n  - 6\n  - 7\n  - 8\n  - 9\n  '11':\n  - 30\n  - 31\n  - 32\n  - 33\n  - 34\n  - 35\n  - 36\n  - 37\n  - 38\n  - 39\n  '110':\n  - 60\n  - 61\n  - 62\n  - 63\n  - 64\n  - 65\n  - 66\n  - 67\n  - 68\n  - 69\n  '1':\n  - 10\n  - 11\n  - 12\n  - 13\n  - 14\n  - 15\n  - 16\n  - 17\n  - 18\n  - 19\n  '100':\n  - 40\n  - 41\n  - 42\n  - 43\n  - 44\n  - 45\n  - 46\n  - 47\n  - 48\n  - 49\n  '111':\n  - 70\n  - 71\n  - 72\n  - 73\n  - 74\n  - 75\n  - 76\n  - 77\n  - 78\n  - 79\n  '101':\n  - 50\n  - 51\n  - 52\n  - 53\n  - 54\n  - 55\n  - 56\n  - 57\n  - 58\n  - 59\n  '1001':\n  - 90\n  - 91\n  - 92\n  - 93\n  - 94\n  - 95\n  - 96\n  - 97\n  - 98\n  - 99\n  '1000':\n  - 80\n  - 81\n  - 82\n  - 83\n  - 84\n  - 85\n  - 86\n  - 87\n  - 88\n  - 89\n  '10':\n  - 20\n  - 21\n  - 22\n  - 23\n  - 24\n  - 25\n  - 26\n  - 27\n  - 28\n  - 29\n";
        assertEquals(ans, yml);

        YamlConfiguration parse = YamlConfiguration.loadConfiguration(new StringReader(yml));
        obj2.deserialize(parse);
        for (int i=0;i<=9;i++) {
            for (int j=0;j<=9;j++)
                assertEquals((long)(i*10+j), (long)obj2.nestedList.get(Integer.toString(i,2)).get(j));
        }
    }

    static class Test2Class implements ISerializable {
        @Serializable
        List<Object> objs = new ArrayList<>();

        Test2Class() {
            objs.add(new DataObject());
            objs.add(new DataObject());
            objs.add(new DataObject.NestedObject());
        }
    }

    @Test
    public void test2() throws Exception {
        YamlConfiguration cfg = new YamlConfiguration();
        Test2Class cls = new Test2Class();
        ((DataObject) cls.objs.get(1)).fillNestedList();
        cls.serialize(cfg);
        //System.out.println(cfg.saveToString());
        cls.objs = null;
        cls.deserialize(YamlConfiguration.loadConfiguration(new StringReader(cfg.saveToString())));
        assertTrue(List.class.isAssignableFrom(cls.objs.getClass()));
        assertEquals(DataObject.class, cls.objs.get(0).getClass());
        assertEquals(DataObject.class, cls.objs.get(1).getClass());
        assertEquals(DataObject.NestedObject.class, cls.objs.get(2).getClass());
        for (int i = 0; i <= 9; i++) {
            for (int j = 0; j <= 9; j++)
                assertEquals((long) (i * 10 + j), (long) ((DataObject) cls.objs.get(1)).nestedList.get(Integer.toString(i, 2)).get(j));
        }
    }
}