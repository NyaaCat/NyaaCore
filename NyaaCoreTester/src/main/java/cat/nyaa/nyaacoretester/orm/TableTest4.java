package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.Objects;

@Table("test4")
public class TableTest4 {
    @Column(primary = true)
    public long key;

    @Column
    public long data1;

    @Column
    public long data2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableTest4 that = (TableTest4) o;
        return key == that.key &&
                data1 == that.data1 &&
                data2 == that.data2;
    }
}