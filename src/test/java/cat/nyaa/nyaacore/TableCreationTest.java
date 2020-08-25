package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.nyaacore.orm.backends.SQLiteDatabase;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TableCreationTest {
    @Test
    public void autoIncrementTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getTableCreationSql1 = SQLiteDatabase.class.getDeclaredMethod("getTableCreationSql", Class.class);
        getTableCreationSql1.setAccessible(true);
        Object invoke = getTableCreationSql1.invoke(null, TestTable.class);
//        assertEquals(invoke, "CREATE TABLE IF NOT EXISTS testTable(id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT name LONGTEXT UNIQUE age INT)");
        System.out.println(invoke);
    }

    @Table("testTable")
    static class TestTable {
        @Column(name = "id", primary = true, autoIncrement = true)
        Long autoIncrement;
        @Column(name = "name", unique = true)
        String unique;
        @Column(name = "age")
        int age;

    }
}
