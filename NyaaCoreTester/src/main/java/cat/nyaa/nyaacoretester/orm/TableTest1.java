package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.Objects;
import java.util.UUID;

@Table("test1")
public class TableTest1 {

    @Column(primary = true)
    public Long id;

    @Column
    public String string;

    @Column
    public UUID uuid;

    public UUID uuid_indirect;

    public TableTest1() {
    }

    public TableTest1(Long id, String string, UUID uuid, UUID uuid_indirect) {
        this.id = id;
        this.string = string;
        this.uuid = uuid;
        this.uuid_indirect = uuid_indirect;
    }

    @Column(name = "uuid_indirect")
    public String getUuidIndirect() {
        return uuid_indirect.toString();
    }

    public void setUuidIndirect(String uuid_indirect) {
        this.uuid_indirect = UUID.fromString(uuid_indirect);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableTest1 that = (TableTest1) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(string, that.string) &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(uuid_indirect, that.uuid_indirect);
    }
}
