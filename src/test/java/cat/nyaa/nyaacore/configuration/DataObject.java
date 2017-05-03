package cat.nyaa.nyaacore.configuration;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataObject implements ISerializable {
    public enum TestEnum {
        VALUE_A,
        VALUE_B,
        VALUE_C;
    }

    public static class NestedObject implements ISerializable {
        @Serializable
        public Integer x = 42;
        @Serializable
        public Double d = 42.42;
    }

    @Serializable
    public String str = "The quick brown fox jumps over the lazy dog";
    @Serializable
    public int number_int = 42;
    @Serializable
    public long number_long = 42;
    @Serializable
    public TestEnum enum_1 = TestEnum.VALUE_A;
    @Serializable
    public Material enum_2 = Material.LEAVES;
    @Serializable
    public Map<String, String> map = new HashMap<>();
    @Serializable
    public NestedObject anotherDataObject = new NestedObject();
    @Serializable
    public Map<String, NestedObject> mapOfDataObject = new HashMap<>();
    @Serializable
    public Map<String, List<Integer>> nestedList = new HashMap<>();

    public DataObject() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        map.put("key4", "value4");
        mapOfDataObject.put("obj1", new NestedObject());
        mapOfDataObject.put("obj2", new NestedObject());
    }

    public void fillNestedList() {
        for (int i=0;i<10;i++) {
            List<Integer> l = new ArrayList<>();
            for (int j=0;j<10;j++) l.add(i*10+j);
            nestedList.put(Integer.toString(i,2), l);
        }
    }
}
