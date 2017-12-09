package cat.nyaa.nyaacore.utils;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

public final class ItemStackUtils {
    /**
     * Get the binary representation of ItemStack
     * for fast ItemStack serialization
     *
     * @param itemStack the item to be serialized
     * @return binary NBT representation of the item stack
     */
    public static byte[] itemToBinary(ItemStack itemStack) throws ReflectiveOperationException, IOException {
        Class<?> classCraftItemStack = ReflectionUtils.getOBCClass("inventory.CraftItemStack");
        Class<?> classNativeItemStack = ReflectionUtils.getNMSClass("ItemStack");
        Class<?> classNBTTagCompound = ReflectionUtils.getNMSClass("NBTTagCompound");

        Method asNMSCopy_craftItemStack = ReflectionUtils.getMethod(classCraftItemStack, "asNMSCopy", ItemStack.class);
        Method save_nativeItemStack = ReflectionUtils.getMethod(classNativeItemStack, "save", classNBTTagCompound);
        Method write_nbtTagCompound = ReflectionUtils.getMethod(classNBTTagCompound, "write", DataOutput.class);

        Object nativeItemStack = asNMSCopy_craftItemStack.invoke(null, itemStack);
        Object nbtTagCompound = classNBTTagCompound.newInstance();
        save_nativeItemStack.invoke(nativeItemStack, nbtTagCompound);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        write_nbtTagCompound.invoke(nbtTagCompound, dos);
        byte[] outputByteArray = baos.toByteArray();
        dos.close();
        baos.close();
        return outputByteArray;
    }

    private static Object unlimitedNBTReadLimiter = null;

    /**
     * Get the ItemStack from its binary representation
     * for fast ItemStack deserialization
     *
     * @param nbt binary item nbt data
     * @return constructed item
     */
    public static ItemStack itemFromBinary(byte[] nbt) throws ReflectiveOperationException, IOException {
        return itemFromBinary(nbt, 0, nbt.length);
    }

    public static ItemStack itemFromBinary(byte[] nbt, int offset, int len) throws ReflectiveOperationException, IOException {
        Class<?> classNBTReadLimiter = ReflectionUtils.getNMSClass("NBTReadLimiter");
        if (unlimitedNBTReadLimiter == null) {
            for (Field f : classNBTReadLimiter.getDeclaredFields()) {
                if (f.getType().equals(classNBTReadLimiter)) {
                    unlimitedNBTReadLimiter = f.get(null);
                    break;
                }
            }
        }

        Class<?> classCraftItemStack = ReflectionUtils.getOBCClass("inventory.CraftItemStack");
        Class<?> classNativeItemStack = ReflectionUtils.getNMSClass("ItemStack");
        Class<?> classNBTTagCompound = ReflectionUtils.getNMSClass("NBTTagCompound");

        Method load_nbtTagCompound = ReflectionUtils.getMethod(classNBTTagCompound, "load", DataInput.class, int.class, classNBTReadLimiter);
        Constructor constructNativeItemStackFromNBTTagCompound = classNativeItemStack.getConstructor(classNBTTagCompound);
        Method asBukkitCopy_CraftItemStack = ReflectionUtils.getMethod(classCraftItemStack, "asBukkitCopy", classNativeItemStack);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nbt, offset, len);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        Object reconstructedNBTTagCompound = classNBTTagCompound.newInstance();
        load_nbtTagCompound.invoke(reconstructedNBTTagCompound, dataInputStream, 0, unlimitedNBTReadLimiter);
        dataInputStream.close();
        byteArrayInputStream.close();
        Object reconstructedNativeItemStack = constructNativeItemStackFromNBTTagCompound.newInstance(reconstructedNBTTagCompound);
        return (ItemStack) asBukkitCopy_CraftItemStack.invoke(null, reconstructedNativeItemStack);
    }

    private static byte[] compress(byte[] data) {
        byte[] ret;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteStreams.copy(new DeflaterInputStream(bis), bos);
            ret = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }

    private static byte[] decompress(byte[] data) {
        byte[] ret;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteStreams.copy(new InflaterInputStream(bis), bos);
            ret = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }

    /* *
     * Structure of binary NBT list:
     * - First byte (n): number of items (currently limit to 0<=n<=127 i.e. MSB=0)
     * - Next 4*n bytes (s1~sn): size of binary nbt for each item
     * - Next sum(s1~sn) bytes: actual data nbt
     */

    /**
     * Convert a list of items into compressed base64 string
     */
    public static String itemsToBase64(List<ItemStack> items) {
        if (items.size() <= 0) return "";
        if (items.size() > 127) {
            throw new IllegalArgumentException("Too many items");
        }

        List<byte[]> nbts = new ArrayList<>();
        for (ItemStack item : items) {
            try {
                nbts.add(itemToBinary(item));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        byte[] uncompressed_binary;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeByte(items.size());
            for (byte[] nbt : nbts) dos.writeInt(nbt.length);
            for (byte[] nbt : nbts) dos.write(nbt);
            uncompressed_binary = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return BaseEncoding.base64().encode(compress(uncompressed_binary));
    }

    /**
     * Convert base64 string back to a list of items
     */
    public static List<ItemStack> itemsFromBase64(String base64) {
        if (base64.length() <= 0) return new ArrayList<>();

        byte[] uncompressed_binary = decompress(BaseEncoding.base64().decode(base64));
        List<ItemStack> ret = new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(uncompressed_binary);
             DataInputStream dis = new DataInputStream(bis)) {
            int n = dis.readByte();
            int[] nbt_length = new int[n];
            for (int i = 0; i < n; i++) nbt_length[i] = dis.readInt();
            for (int i = 0; i < n; i++) {
                byte[] tmp = new byte[nbt_length[i]];
                dis.readFully(tmp);
                ret.add(itemFromBinary(tmp));
            }
        } catch (IOException | ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }

    public static String itemToBase64(ItemStack item) {
        if (item == null) throw new IllegalArgumentException();
        return itemsToBase64(Collections.singletonList(item));
    }

    public static ItemStack itemFromBase64(String base64) {
        if (base64 == null) throw new IllegalArgumentException();
        List<ItemStack> ret = itemsFromBase64(base64);
        if (ret != null && ret.size() >= 1) return ret.get(0);
        return null;
    }

    /**
     * https://github.com/sainttx/Auctions/blob/12533c9af0b1dba700473bf728895abb9ff5b33b/Auctions/src/main/java/com/sainttx/auctions/SimpleMessageFactory.java#L197
     * Convert an item to its JSON representation to be shown in chat.
     * NOTE: this method has no corresponding deserializer.
     */
    public static String itemToJson(ItemStack itemStack) throws RuntimeException {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
        Class<?> craftItemStackClazz = ReflectionUtils.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtils.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
        Class<?> nmsItemStackClazz = ReflectionUtils.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtils.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtils.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
        Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            throw new RuntimeException("failed to serialize itemstack to nms item", t);
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }
}
