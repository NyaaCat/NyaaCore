package cat.nyaa.nyaacore.database;

import java.util.UUID;

@DataTable("test1")
public class TestTable {
    @DataColumn
    @PrimaryKey
    public Long id;

    @DataColumn
    public String string;

    @DataColumn
    public UUID uuid;

    public UUID uuid_indirect;

    @DataColumn("uuid_indirect")
    public String getUuidIndirect() {
        return uuid_indirect.toString();
    }

    public void setUuidIndirect(String uuid_indirect) {
        this.uuid_indirect = UUID.fromString(uuid_indirect);
    }
}
