package cat.nyaa.nyaacore.utils;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Optional;

public class ItemTagUtils {

    public static Optional<String> getString(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getString(key);
    }

    public static Optional<String> setString(ItemStack item, String key, String value) throws NoSuchFieldException, IllegalAccessException {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putString(key, value));
        return Optional.of(value);
    }

    public static Optional<Integer> getInt(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getInt(key);
    }

    public static Optional<Integer> setInt(ItemStack item, String key, int value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putInt(key, value));
        return Optional.of(value);
    }

    public static Optional<Double> getDouble(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getDouble(key);
    }

    public static Optional<Double> setDouble(ItemStack item, String key, double value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putDouble(key, value));
        return Optional.of(value);
    }

    public static Optional<Short> getShort(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getShort(key);
    }

    public static Optional<Short> setShort(ItemStack item, String key, short value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putShort(key, value));
        return Optional.of(value);
    }

    public static Optional<Byte> getByte(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getByte(key);
    }

    public static Optional<Byte> setByte(ItemStack item, String key, byte value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putByte(key, value));
        return Optional.of(value);
    }

    public static Optional<Long> getLong(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getLong(key);
    }

    public static Optional<Long> setLong(ItemStack item, String key, long value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putLong(key, value));
        return Optional.of(value);
    }

    public static Optional<long[]> getLongArray(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getLongArray(key);
    }

    public static Optional<long[]> setLongArray(ItemStack item, String key, long[] value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putLongArray(key, value));
        return Optional.of(value);
    }

    public static Optional<int[]> getIntArray(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getIntArray(key);
    }

    public static Optional<int[]> setIntArray(ItemStack item, String key, int[] value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putIntArray(key, value));
        return Optional.of(value);
    }

    public static Optional<byte[]> getByteArray(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getByteArray(key);
    }

    public static Optional<byte[]> setByteArray(ItemStack item, String key, byte[] value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putByteArray(key, value));
        return Optional.of(value);
    }

    public static Optional<Boolean> getBoolean(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getBoolean(key);
    }

    public static Optional<Boolean> setBoolean(ItemStack item, String key, boolean value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putBoolean(key, value));
        return Optional.of(value);
    }

    public static Optional<Float> getFloat(ItemStack item, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(item);
        CompoundTag tag = getTag(nmsItem);
        if (tag == null) return Optional.empty();
        return !tag.contains(key) ? Optional.empty() : tag.getFloat(key);
    }

    public static Optional<Float> setFloat(ItemStack item, String key, float value) {
        net.minecraft.world.item.ItemStack nmsItem = null;
        if(item instanceof CraftItemStack) {
            nmsItem = CraftItemStack.unwrap(item);
        }
        if (nmsItem == null) {
            return Optional.empty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, nmsItem, (tag) -> tag.putFloat(key, value));
        return Optional.of(value);
    }

    private static CompoundTag getTag(net.minecraft.world.item.ItemStack itemStack) {
        DataComponentMap components = itemStack.getComponents();
        CustomData customData = components.get(DataComponents.CUSTOM_DATA);
        if(customData == null) {
            return null;
        }
        return customData.getUnsafe();
    }
}
