package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Table("all_types")
public class TableAllTypes {
    /*
     * There are all accepted java types:
     *   1. bool/Boolean  => INTEGER  [true=1, false=0]
     *   2. int/Integer   => INTEGER  [no conversion]
     *   3. long/Long     => BIGINT   [no conversion]
     *   4. float/Float   => FLOAT    [no conversion]
     *   5. double/Double => DOUBLE   [no conversion]
     *   6. String        => MEDIUMTEXT     [no conversion]
     *   7. Enum          => MEDIUMTEXT     [name() and valueOf()]
     *   8. ItemStack     => MEDIUMTEXT     [nbt(de)serializebase64()]
     *   9. Any type can be serialized/deserialized using toString() and fromString()/parse() (e.g. ZonedDateTime)
     *                    => MEDIUMTEXT     [toString() and fromString()/parse()]
     */
    @Column
    boolean p_field_bool;
    @Column
    Boolean o_field_bool;
    boolean p_method_bool;
    Boolean o_method_bool;

    @Column
    int p_field_int;
    @Column
    Integer o_field_int;
    int p_method_int;
    Integer o_method_int;

    @Column
    long p_field_long;
    @Column
    Long o_field_long;
    long p_method_long;
    Long o_method_long;

    @Column
    float p_field_float;
    @Column
    Float o_field_float;
    float p_method_float;
    Float o_method_float;

    @Column
    double p_field_double;
    @Column
    Double o_field_double;
    double p_method_double;
    Double o_method_double;

    @Column
    String o_field_string;
    String o_method_string;

    @Column
    DemoEnums o_field_enum;
    DemoEnums o_method_enum;

    @Column
    ItemStack o_field_itemstack;
    ItemStack o_method_itemstack;

    @Column
    UUID o_field_uuid;
    UUID o_method_uuid;

    @Column
    ZonedDateTime o_field_zoneddatetime;
    ZonedDateTime o_method_zoneddatetime;

    public static TableAllTypes instance() {
        TableAllTypes ret = new TableAllTypes();
        ret.p_field_bool = false;
        ret.o_field_bool = true;
        ret.p_method_bool = true;
        ret.o_method_bool = false;
        ret.p_field_int = 43;
        ret.o_field_int = 44;
        ret.p_method_int = 45;
        ret.o_method_int = 46;
        ret.p_field_long = 47;
        ret.o_field_long = 48L;
        ret.p_method_long = 49;
        ret.o_method_long = 50L;
        ret.p_field_float = 51.51F;
        ret.o_field_float = 52.52F;
        ret.p_method_float = 53.53F;
        ret.o_method_float = 54.54F;
        ret.p_field_double = 55.55D;
        ret.o_field_double = 56.56D;
        ret.p_method_double = 57.57D;
        ret.o_method_double = 58.58D;
        ret.o_field_string = "FFFS";
        ret.o_method_string = "FFFE";
        ret.o_field_enum = DemoEnums.VALUE_1;
        ret.o_method_enum = DemoEnums.VALUE_2;
        ret.o_field_itemstack = new ItemStack(Material.OAK_WOOD);
        ret.o_method_itemstack = new ItemStack(Material.CHEST, 32);
        ret.o_field_uuid = UUID.randomUUID();
        ret.o_method_uuid = UUID.randomUUID();
        ret.o_field_zoneddatetime = ZonedDateTime.now();
        ret.o_method_zoneddatetime = ret.o_field_zoneddatetime.plus(442L, ChronoUnit.SECONDS);
        return ret;
    }

    @Column
    public boolean getP_method_bool() {
        return p_method_bool;
    }

    public void setP_method_bool(boolean p_method_bool) {
        this.p_method_bool = p_method_bool;
    }

    @Column
    public Boolean getO_method_bool() {
        return o_method_bool;
    }

    public void setO_method_bool(Boolean o_method_bool) {
        this.o_method_bool = o_method_bool;
    }

    @Column
    public int getP_method_int() {
        return p_method_int;
    }

    public void setP_method_int(int p_method_int) {
        this.p_method_int = p_method_int;
    }

    @Column
    public Integer getO_method_int() {
        return o_method_int;
    }

    public void setO_method_int(Integer o_method_int) {
        this.o_method_int = o_method_int;
    }

    @Column
    public long getP_method_long() {
        return p_method_long;
    }

    public void setP_method_long(long p_method_long) {
        this.p_method_long = p_method_long;
    }

    @Column
    public Long getO_method_long() {
        return o_method_long;
    }

    public void setO_method_long(Long o_method_long) {
        this.o_method_long = o_method_long;
    }

    @Column
    public float getP_method_float() {
        return p_method_float;
    }

    public void setP_method_float(float p_method_float) {
        this.p_method_float = p_method_float;
    }

    @Column
    public Float getO_method_float() {
        return o_method_float;
    }

    public void setO_method_float(Float o_method_float) {
        this.o_method_float = o_method_float;
    }

    @Column
    public double getP_method_double() {
        return p_method_double;
    }

    public void setP_method_double(double p_method_double) {
        this.p_method_double = p_method_double;
    }

    @Column
    public Double getO_method_double() {
        return o_method_double;
    }

    public void setO_method_double(Double o_method_double) {
        this.o_method_double = o_method_double;
    }

    @Column
    public String getO_method_string() {
        return o_method_string;
    }

    public void setO_method_string(String o_method_string) {
        this.o_method_string = o_method_string;
    }

    @Column
    public DemoEnums getO_method_enum() {
        return o_method_enum;
    }

    public void setO_method_enum(DemoEnums o_method_enum) {
        this.o_method_enum = o_method_enum;
    }

    @Column
    public ItemStack getO_method_itemstack() {
        return o_method_itemstack;
    }

    public void setO_method_itemstack(ItemStack o_method_itemstack) {
        this.o_method_itemstack = o_method_itemstack;
    }

    @Column
    public UUID getO_method_uuid() {
        return o_method_uuid;
    }

    public void setO_method_uuid(UUID o_method_uuid) {
        this.o_method_uuid = o_method_uuid;
    }

    @Column
    public ZonedDateTime getO_method_zoneddatetime() {
        return o_method_zoneddatetime;
    }

    public void setO_method_zoneddatetime(ZonedDateTime o_method_zoneddatetime) {
        this.o_method_zoneddatetime = o_method_zoneddatetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableAllTypes that = (TableAllTypes) o;
        return p_field_bool == that.p_field_bool &&
                p_method_bool == that.p_method_bool &&
                p_field_int == that.p_field_int &&
                p_method_int == that.p_method_int &&
                p_field_long == that.p_field_long &&
                p_method_long == that.p_method_long &&
                Float.compare(that.p_field_float, p_field_float) == 0 &&
                Float.compare(that.p_method_float, p_method_float) == 0 &&
                Double.compare(that.p_field_double, p_field_double) == 0 &&
                Double.compare(that.p_method_double, p_method_double) == 0 &&
                Objects.equals(o_field_bool, that.o_field_bool) &&
                Objects.equals(o_method_bool, that.o_method_bool) &&
                Objects.equals(o_field_int, that.o_field_int) &&
                Objects.equals(o_method_int, that.o_method_int) &&
                Objects.equals(o_field_long, that.o_field_long) &&
                Objects.equals(o_method_long, that.o_method_long) &&
                Objects.equals(o_field_float, that.o_field_float) &&
                Objects.equals(o_method_float, that.o_method_float) &&
                Objects.equals(o_field_double, that.o_field_double) &&
                Objects.equals(o_method_double, that.o_method_double) &&
                Objects.equals(o_field_string, that.o_field_string) &&
                Objects.equals(o_method_string, that.o_method_string) &&
                o_field_enum == that.o_field_enum &&
                o_method_enum == that.o_method_enum &&
                Objects.equals(o_field_itemstack, that.o_field_itemstack) &&
                Objects.equals(o_method_itemstack, that.o_method_itemstack) &&
                Objects.equals(o_field_uuid, that.o_field_uuid) &&
                Objects.equals(o_method_uuid, that.o_method_uuid) &&
                Objects.equals(o_field_zoneddatetime, that.o_field_zoneddatetime) &&
                Objects.equals(o_method_zoneddatetime, that.o_method_zoneddatetime);
    }

    public enum DemoEnums {
        VALUE_1,
        VALUE_2,
        VALUE_3;
    }
}
