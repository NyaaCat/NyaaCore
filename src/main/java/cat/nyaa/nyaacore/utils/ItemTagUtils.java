package cat.nyaa.nyaacore.utils;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Optional;

public class ItemTagUtils {

    static Field handle;

    public static Optional<String> getString(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getString(key));
    }

    public static Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Integer> getInt(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getInt(key));
    }

    public static Optional<Integer> setInt(ItemStack item, String key, int value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putInt(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Double> getDouble(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getDouble(key));
    }

    public static Optional<Double> setDouble(ItemStack item, String key, double value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putDouble(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Short> getShort(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getShort(key));
    }

    public static Optional<Short> setShort(ItemStack item, String key, short value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putShort(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Byte> getByte(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getByte(key));
    }

    public static Optional<Byte> setByte(ItemStack item, String key, byte value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putByte(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Long> getLong(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getLong(key));
    }

    public static Optional<Long> setLong(ItemStack item, String key, long value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putLong(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<long[]> getLongArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getLongArray(key));
    }

    public static Optional<long[]> setLongArray(ItemStack item, String key, long[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putLongArray(key, value); // this is anonymous
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<int[]> getIntArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getIntArray(key));
    }

    public static Optional<int[]> setIntArray(ItemStack item, String key, int[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putIntArray(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<byte[]> getByteArray(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getByteArray(key));
    }

    public static Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putByteArray(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Boolean> getBoolean(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getBoolean(key));
    }

    public static Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putBoolean(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    public static Optional<Float> getFloat(ItemStack item, String key) {
        Optional<net.minecraft.world.item.ItemStack> item1;
        try {
            item1 = getItem(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
        if (item1.isEmpty()) return Optional.empty();
        CompoundTag tag = item1.get().getTag();
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : Optional.of(tag.getFloat(key));
    }

    public static Optional<Float> setFloat(ItemStack item, String key, float value) throws NoSuchFieldException, IllegalAccessException {
        Optional<net.minecraft.world.item.ItemStack> is = getItem(item);
        if (is.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.item.ItemStack itemStack = is.get();
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag == null) return Optional.empty();
        tag.putFloat(key, value);
        itemStack.setTag(tag);
        return Optional.of(value);
    }

    private static Optional<net.minecraft.world.item.ItemStack> getItem(ItemStack itemStack) throws NoSuchFieldException, IllegalAccessException {
        if (!(itemStack instanceof CraftItemStack)) {
            return Optional.empty();
        }
        if (handle == null) {
            handle = CraftItemStack.class.getDeclaredField("handle");
        }
        handle.setAccessible(true);
        return Optional.ofNullable((net.minecraft.world.item.ItemStack) handle.get(itemStack));
    }
}
