package cat.nyaa.nyaacore.utils;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ItemTagUtils {
    private static final int TYPE_NUMBER = 99;
    private static final int TYPE_STRING = 8;
    private static final int TYPE_BYTE_ARRAY = 7;
    private static final int TYPE_INT_ARRAY = 11;
    private static final int TYPE_LONG_ARRAY = 12;
    private static final int TYPE_TAG_COMPOUND = 12;


    public static Optional<String> getString(ItemStack item, String key) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_STRING) ? Optional.empty() : Optional.of(tag.getString(key));
    }
    public static Optional<String> setString(ItemStack item, String key, String value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        tag.setString(key, value);
        return Optional.of(value);
    }

    public static Optional<Integer> getInt(ItemStack item, String key) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_NUMBER) ? Optional.empty() : Optional.of(tag.getInt(key));
    }
    public static Optional<Integer> setInt(ItemStack item, String key, int value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setInt(key, value);
        return Optional.of(value);
    }


    public static Optional<Double> getDouble(ItemStack item, String key) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_NUMBER) ? Optional.empty() : Optional.of(tag.getDouble(key));
    }
    public static Optional<Double> setDouble(ItemStack item, String key, double value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setDouble(key, value);
        return Optional.of(value);
    }


    public static Optional<Short> getShort(ItemStack item, String key) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_NUMBER) ? Optional.empty() : Optional.of(tag.getShort(key));
    }
    public static Optional<Short> setShort(ItemStack item, String key, short value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setShort(key, value);
        return Optional.of(value);
    }


    public static Optional<Byte> getByte(ItemStack item, String key) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_NUMBER) ? Optional.empty() : Optional.of(tag.getByte(key));
    }
    public static Optional<Byte> setByte(ItemStack item, String key, byte value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setByte(key, value);
        return Optional.of(value);
    }


    public static Optional<Long> getLong(ItemStack item, String key){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null)return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_NUMBER) ? Optional.empty() : Optional.of(tag.getLong(key));
    }
    public static Optional<Long> setLong(ItemStack item, String key, long value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setLong(key, value);
        return Optional.of(value);
    }


    public static Optional<long[]> getLongArray(ItemStack item, String key) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_LONG_ARRAY) ? Optional.empty() : Optional.of(tag.getLongArray(key));
    }
    public static Optional<long[]> setLongArray(ItemStack item, String key, long[] value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.a(key, value); // this is anonymous
        return Optional.of(value);
    }


    public static Optional<int[]> getIntArray(ItemStack item, String key){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null)return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_INT_ARRAY) ? Optional.empty() : Optional.of(tag.getIntArray(key));
    }
    public static Optional<int[]> setIntArray(ItemStack item, String key, int[] value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setIntArray(key, value);
        return Optional.of(value);
    }


    public static Optional<byte[]> getByteArray(ItemStack item, String key){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null)return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_BYTE_ARRAY) ? Optional.empty() : Optional.of(tag.getByteArray(key));
    }
    public static Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setByteArray(key, value);
        return Optional.of(value);
    }


    public static Optional<Boolean> getBoolean(ItemStack item, String key){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null)return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_NUMBER) ? Optional.empty() : Optional.of(tag.getBoolean(key));
    }
    public static Optional<Boolean> setBoolean(ItemStack item, String key, boolean value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setBoolean(key, value);
        return Optional.of(value);
    }


    public static Optional<Float> getFloat(ItemStack item, String key){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null)return Optional.empty();
        return !tag.hasKeyOfType(key, TYPE_NUMBER) ? Optional.empty() : Optional.of(tag.getFloat(key));
    }
    public static Optional<Float> setFloat(ItemStack item, String key, float value){
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setFloat(key, value);
        return Optional.of(value);
    }
}
