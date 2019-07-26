package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.Objects;

@Table("test6")
public class TableTest6 {
    @Column(primary = true)
    String key;
    @Column(nullable = true)
    String value;

    public TableTest6() {
    }

    public TableTest6(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableTest6 that = (TableTest6) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }
}
