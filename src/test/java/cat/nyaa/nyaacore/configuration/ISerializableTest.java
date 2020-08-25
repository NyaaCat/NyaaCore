package cat.nyaa.nyaacore.configuration;

import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.junit.Test;

import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.*;

public class ISerializableTest {
    private static final boolean enable_print = false;
    //private static final boolean enable_print = true;

    private <T extends ISerializable> T process(T obj) throws ReflectiveOperationException {
        YamlConfiguration cfg = new YamlConfiguration();
        obj.serialize(cfg);
        T newObj = (T) obj.getClass().newInstance();
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

    @Test
    public void test1() throws Exception {
        process(new Test1Class().fill()).assertEq();
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

    @Test
    public void test3() throws Exception {
        Test3Class o = new Test3Class();
        o.map = new HashMap<>();
        for (int i = 0; i <= 10; i++) o.map.put(Integer.toString(i), i);
        Test3Class p = process(o);
        for (int i = 0; i <= 10; i++) assertEquals((Integer) i, p.map.get(Integer.toString(i)));
    }

    @Test
    public void test4() throws Exception {
        process(new Test4Class(10)).check(10);
    }

    @Test
    public void test5() throws Exception {
        Test5Class c = new Test5Class();
        c.map = new HashMap<>();
        c.map.put("abc", new Test1Class().fill());
        process(c).map.get("abc").assertEq();
    }

    @Test
    public void test6() throws Exception {
        process(new Test6Class(6)).verify(6);
    }

    @Test
    public void test7() throws Exception {
        process(new Test7Class().fill()).verify();
    }

    //test extend and LinkedHashMap.
    @Test
    public void test8() throws Exception {
        Test8Class obj = new Test8Class();
        obj.fill();
        Test8Class fill = process(obj);
        fill.verify();
    }

    //    @Test
    public void test9() throws ReflectiveOperationException {
        Test9Class test9Class = new Test9Class();
        test9Class.fill();
        process(test9Class).verify();
    }

    @Test
    public void test10() throws ReflectiveOperationException {
        Test10Class test10Class = new Test10Class();
        test10Class.test7Class.fill();
        test10Class.innerClass.test8Class.fill();
        process(test10Class);
        test10Class.test7Class.verify();
        test10Class.test7Class.verify();
    }

    public enum TestEnum {
        VALUE_A,
        VALUE_B,
        VALUE_C,
        VALUE_D,
        VALUE_E
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
            assertEquals((Integer) 42, i);
            assertEquals((Double) 42.42, d);
            assertEquals("42", s);
            assertEquals(TestEnum.VALUE_D, e);
        }
    }

    static class Test2Class implements ISerializable {
        @Serializable(name = "object.nested.double")
        Test1Class obj;
    }

    static class Test3Class implements ISerializable {
        @Serializable
        Map<String, Integer> map;
    }

    static class Test4Class implements ISerializable {
        @Serializable
        int depth;
        @Serializable
        Test4Class nested;

        public Test4Class() {
        }

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

    static class Test5Class implements ISerializable {
        @Serializable
        Map<String, Test1Class> map;
    }

    static class Test6Class implements ISerializable {
        @Serializable(manualSerialization = true)
        List<Test6Class> list = null;

        public Test6Class() {
        }

        public Test6Class(int depth) {
            if (depth == 0) return;
            list = new ArrayList<>();
            for (int i = 0; i < depth; i++) {
                list.add(new Test6Class(depth - 1));
            }
        }

        public void verify(int depth) {
            if (depth == 0) assertNull(list);
            else {
                assertNotNull(list);
                assertEquals(depth, list.size());
                for (int i = 0; i < depth; i++) list.get(i).verify(depth - 1);
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
            for (int i = 0; i < list.size(); i++) {
                ConfigurationSection sec = config.createSection(Integer.toString(i));
                list.get(i).serialize(sec);
            }
        }
    }

    static class Test7Class implements ISerializable {
        @Serializable(name = "list.strings")
        List<String> s;
        @Serializable(name = "list.numbers")
        List<Integer> i;
        @Serializable(name = "map.testMap")
        Map<String, String> stringMap;

        public Test7Class fill() {
            this.s = new ArrayList<>();
            this.i = new ArrayList<>();
            for (int x = 0; x < 100; x++) {
                i.add(x);
                s.add(Integer.toString(x));
            }
            return this;
        }

        public void verify() {
            for (Integer x = 0; x < 100; x++) {
                assertEquals(x, this.i.get(x));
                assertEquals(x.toString(), this.s.get(x));
            }
        }
    }

    static class Test8Class extends Test7Class {
        @Serializable
        String name;

        @Override
        public Test8Class fill() {
            super.fill();
            name = "tester";
            stringMap = new LinkedHashMap<>();
            for (int j = 99, i = 0; j >= 0; j--, i++) {
                stringMap.put(String.valueOf(j), String.valueOf(i));
            }
            return this;
        }

        @Override
        public void verify() {
            super.verify();
            assertEquals(this.name, "tester");
            int i = 0, j = 99;
            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                String key = entry.getKey();
                String s2 = entry.getValue();
                assertEquals(key, String.valueOf(j));
                assertEquals(s2, String.valueOf(i));
                i++;
                j--;
            }
        }
    }

    static class Test9Class implements ISerializable {
        @Serializable
        public String str = "";
        @Serializable
        public int in = 0;
        @Serializable
        public Material material = Material.AIR;
        @Serializable
        public List<EntityType> entityTypes = new ArrayList<>();
        @Serializable
        public Map<String, Difficulty> difficultyMap = new LinkedHashMap<>();

        {
            entityTypes.add(EntityType.BAT);
            difficultyMap.put("100", Difficulty.EASY);
        }

        public void fill() {
            str = "laji";
            in = 100;
            material = Material.ARROW;
            entityTypes.clear();
            entityTypes.add(EntityType.BLAZE);
            difficultyMap.put("10", Difficulty.HARD);
        }

        public void verify() {
            assertEquals(str, "laji");
            assertEquals(in, 100);
            assertEquals(entityTypes.size(), 1);
            assertEquals(entityTypes.get(0), EntityType.BLAZE);
            assertEquals(difficultyMap.size(), 2);
            Set<Map.Entry<String, Difficulty>> entries = difficultyMap.entrySet();
            Iterator<Map.Entry<String, Difficulty>> iterator = entries.iterator();
            assertEquals(iterator.next().getKey(), "100");
            assertEquals(iterator.next().getKey(), "10");
        }
    }

    static class Test10Class implements ISerializable {
        @Serializable
        Test7Class test7Class = new Test7Class();
        @Serializable
        Test10InnerClass innerClass = new Test10InnerClass();

        static class Test10InnerClass implements ISerializable {
            @Serializable
            Test8Class test8Class = new Test8Class();
        }

    }

}

