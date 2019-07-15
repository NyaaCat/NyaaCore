package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@Table("test2")
public class TableTest2 {
    @Column(primary = true)
    public long id;

    @Column
    public ItemStack item;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableTest2 that = (TableTest2) o;
        return id == that.id &&
                Objects.equals(item, that.item);
    }
}
