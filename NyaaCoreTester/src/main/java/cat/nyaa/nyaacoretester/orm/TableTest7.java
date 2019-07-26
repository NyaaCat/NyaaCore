package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;


@Table("test7")
public class TableTest7 {
    @Column(primary = true)
    int id;
    @Column(unique = false)
    long val;
    @Column(unique = true)
    long val_uniq;

    public TableTest7() {
    }

    public TableTest7(int id, long val, long val_uniq) {
        this.id = id;
        this.val = val;
        this.val_uniq = val_uniq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableTest7 that = (TableTest7) o;
        return id == that.id &&
                val == that.val &&
                val_uniq == that.val_uniq;
    }
}
