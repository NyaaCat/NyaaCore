package cat.nyaa.nyaacore.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        List<Test1Class> list;
    }

    @Test
    public void test3() throws Exception {
        Test3Class o = new Test3Class();
        o.list = new LinkedList<>();
        for (int i = 0;i<5;i++) o.list.add(new Test1Class().fill());
        Test3Class n = process(o);
        assertEquals(5, o.list.size());
        for (int i = 0;i<5;i++) n.list.get(i).assertEq();
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
        int depth;
        @Serializable
        List<Test5Class> nested;
        public Test5Class() {}
        public Test5Class(int depth) {
            this.depth = depth;
            if (depth > 0) {
                nested = Collections.singletonList(new Test5Class(depth - 1));
            }
        }

        public void check(int depth) {
            assertEquals(depth, this.depth);
            if (depth > 0) {
                assertNotNull(nested);
                assertEquals(1, nested.size());
                nested.get(0).check(depth - 1);
            }
        }
    }

    @Test
    public void test5() throws Exception {
        process(new Test5Class(10)).check(10);
    }
}

