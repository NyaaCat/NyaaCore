package cat.nyaa.nyaacore.utils;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Optional;

public class ItemTagUtils {

    static Field handle;

    public static Optional<String> getString(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getString(key));
    }

    public static Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        tag.setString(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Integer> getInt(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getInt(key));
    }

    public static Optional<Integer> setInt(ItemStack item, String key, int value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setInt(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Double> getDouble(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getDouble(key));
    }

    public static Optional<Double> setDouble(ItemStack item, String key, double value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setDouble(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Short> getShort(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getShort(key));
    }

    public static Optional<Short> setShort(ItemStack item, String key, short value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setShort(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Byte> getByte(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getByte(key));
    }

    public static Optional<Byte> setByte(ItemStack item, String key, byte value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setByte(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Long> getLong(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getLong(key));
    }

    public static Optional<Long> setLong(ItemStack item, String key, long value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setLong(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<long[]> getLongArray(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getLongArray(key));
    }

    public static Optional<long[]> setLongArray(ItemStack item, String key, long[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.a(key, value); // this is anonymous
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<int[]> getIntArray(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getIntArray(key));
    }

    public static Optional<int[]> setIntArray(ItemStack item, String key, int[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setIntArray(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<byte[]> getByteArray(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getByteArray(key));
    }

    public static Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setByteArray(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Boolean> getBoolean(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getBoolean(key));
    }

    public static Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setBoolean(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Float> getFloat(ItemStack item, String key) {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> item1 = null;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (!item1.isPresent()) return Optional.empty();
        NBTTagCompound tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.hasKey(key) ? Optional.empty() : Optional.of(tag.getFloat(key));
    }

    public static Optional<Float> setFloat(ItemStack item, String key, float value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.server.v1_16_R3.ItemStack> is = getItem(item);
        if (!is.isPresent()) {
            return Optional.empty();
        }
        net.minecraft.server.v1_16_R3.ItemStack itemStack = is.get();
        NBTTagCompound tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.setFloat(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    private static Optional<net.minecraft.server.v1_16_R3.ItemStack> getItem(ItemStack itemStack) throws NoSuchFieldException, IllegalAccessException {
        if (!(itemStack instanceof CraftItemStack)) {
            return Optional.empty();
        }
        if (handle == null) {
            handle = CraftItemStack.class.getDeclaredField("handle");
        }
        handle.setAccessible(true);
        return Optional.ofNullable((net.minecraft.server.v1_16_R3.ItemStack) handle.get(itemStack));
    }
}
