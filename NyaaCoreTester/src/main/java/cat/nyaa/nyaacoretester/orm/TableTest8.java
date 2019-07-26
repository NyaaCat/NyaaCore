package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.Objects;

@Table("test8")
public class TableTest8 {
    @Column(primary = true)
    Integer x;
    @Column
    String y;

    public TableTest8() {
    }

    public TableTest8(Integer x, String y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableTest8 that = (TableTest8) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y);
    }
}
