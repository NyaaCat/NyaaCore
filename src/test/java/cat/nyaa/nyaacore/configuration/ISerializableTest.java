package cat.nyaa.nyaacore.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ISerializableTest {
    private static final boolean enable_print = false;
    //private static final boolean enable_print = true;

    private <T extends ISerializable> T process(T obj) throws ReflectiveOperationException{
        YamlConfiguration cfg = new YamlConfiguration();
        obj.serialize(cfg);
        T newObj = (T)obj.getClass().newInstance();
        if (enable_print) System.out.println(cfg.saveToString());
        newObj.deserialize(YamlConfiguration.loadConfiguration(new StringReader(cfg.saveToString())));
        return newObj;
    }

    private void print(ISerializable obj) {
        if (!enable_print) return;
        YamlConfiguration cfg = new YamlConfiguration();
        obj.serialize(cfg);
        System.out.println(cfg.saveToString());
    }

    public static enum TestEnum {
        VALUE_A,
        VALUE_B,
        VALUE_C,
        VALUE_D,
        VALUE_E;
    }

    public static class Test1Class implements ISerializable {
        @Serializable
        public Integer i;
        @Serializable
        public Double d;
        @Serializable
        public String s;
        @Serializable
        public TestEnum e;

        public Test1Class fill() {
            i = 42;
            d = 42.42;
            s = "42";
            e = TestEnum.VALUE_D;
            return this;
        }

        public void assertEq() {
            assertEquals((Integer)42, i);
            assertEquals((Double)42.42, d);
            assertEquals("42", s);
            assertEquals(TestEnum.VALUE_D, e);
        }
    }

    @Test
    public void test1() throws Exception {
        process(new Test1Class().fill()).assertEq();
    }

    static class Test2Class implements ISerializable {
        @Serializable(name = "object.nested.double")
        Test1Class obj;
    }

    @Test
    public void test2() throws Exception {
        Test2Class obj = new Test2Class();
        obj.obj = new Test1Class().fill();
        Test2Class n = process(obj);
        assertNotNull(n);
        assertNotNull(n.obj);
        assertEquals(Test2Class.class, n.getClass());
        n.obj.assertEq();
    }

    static class Test3Class implements ISerializable {
        @Serializable
        Map<String, Integer> map;
    }

    @Test
    public void test3() throws Exception {
        Test3Class o = new Test3Class();
        o.map = new HashMap<>();
        for (int i = 0;i<=10;i++) o.map.put(Integer.toString(i), i);
        Test3Class p = process(o);
        for (int i = 0;i<=10;i++) assertEquals((Integer)i, p.map.get(Integer.toString(i)));
    }

    static class Test4Class implements ISerializable {
        @Serializable
        int depth;
        @Serializable
        Test4Class nested;
        public Test4Class() {}
        public Test4Class(int depth) {
            this.depth = depth;
            if (depth > 0) {
                nested = new Test4Class(depth - 1);
            }
        }

        public void check(int depth) {
            assertEquals(depth, this.depth);
            if (depth > 0) {
                assertNotNull(nested);
                nested.check(depth - 1);
            }
        }
    }

    @Test
    public void test4() throws Exception {
        process(new Test4Class(10)).check(10);
    }

    static class Test5Class implements ISerializable {
        @Serializable
        Map<String, Test1Class> map;
    }

    @Test
    public void test5() throws Exception {
        Test5Class c = new Test5Class();
        c.map = new HashMap<>();
        c.map.put("abc", new Test1Class().fill());
        process(c).map.get("abc").assertEq();
    }

    static class Test6Class implements ISerializable {
        List<Test6Class> list = null;
        public Test6Class(){}
        public Test6Class(int depth) {
            if (depth == 0) return;
            list = new ArrayList<>();
            for (int i=0;i<depth;i++) {
                list.add(new Test6Class(depth-1));
            }
        }

        public void verify(int depth) {
            if (depth == 0) assertNull(list);
            else {
                assertNotNull(list);
                assertEquals(depth, list.size());
                for (int i=0;i<depth;i++) list.get(i).verify(depth-1);
            }
        }

        @Override
        public void deserialize(ConfigurationSection config) {
            if (config.getKeys(false).size() == 0) return;
            list = new ArrayList<>();
            List<String> keyList = new ArrayList<>(config.getKeys(false));
            keyList.sort(Comparator.naturalOrder());
            for (String key : keyList) {
                ConfigurationSection sec = config.getConfigurationSection(key);
                Test6Class n = new Test6Class();
                n.deserialize(sec);
                list.add(n);
            }
        }

        @Override
        public void serialize(ConfigurationSection config) {
            if (list == null) return;
            for (int i=0;i<list.size();i++) {
                ConfigurationSection sec = config.createSection(Integer.toString(i));
                list.get(i).serialize(sec);
            }
        }
    }

    @Test
    public void test6() throws Exception {
        process(new Test6Class(6)).verify(6);
    }
}

