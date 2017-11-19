package cat.nyaa.nyaacore.utils;

import com.google.common.io.BaseEncoding;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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

    private static void writeInt(OutputStream s, int x) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) ((x >> 24) & 0xFF);
        b[1] = (byte) ((x >> 16) & 0xFF);
        b[2] = (byte) ((x >> 8) & 0xFF);
        b[3] = (byte) (x & 0xFF);
        s.write(b);
    }

    private static int readInt(byte[] b, int offset) throws IOException {
        if (offset < 0 || offset + 3 >= b.length) throw new IllegalArgumentException("bad offset");
        return (b[offset] << 24) | (b[offset + 1] << 16) | (b[offset + 2] << 8) | b[offset+3];
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

        byte[] uncompressed_binary;
        try (ByteArrayOutputStream uncompressed = new ByteArrayOutputStream()) {
            uncompressed.write(items.size());
            List<byte[]> nbts = new ArrayList<>();
            for (ItemStack item : items) {
                byte[] nbt;
                try {
                    nbt = itemToBinary(item);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                nbts.add(nbt);
            }
            for (byte[] nbt : nbts) writeInt(uncompressed, nbt.length);
            for (byte[] nbt : nbts) uncompressed.write(nbt);
            uncompressed_binary = uncompressed.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        byte[] compressed_binary;
        try (ByteArrayOutputStream compressed = new ByteArrayOutputStream()) {
            Deflater compressor = new Deflater();
            compressor.setInput(uncompressed_binary);
            compressor.finish();
            while (!compressor.finished()) {
                byte[] buf = new byte[1024];
                int len = compressor.deflate(buf);
                compressed.write(buf, 0, len);
            }
            compressor.end();
            compressed_binary = compressed.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return BaseEncoding.base64().encode(compressed_binary);
    }

    /**
     * Convert base64 string back to a list of items
     */
    public static List<ItemStack> itemsFromBase64(String base64) {
        if (base64.length() <= 0) return new ArrayList<>();
        byte[] compressed_binary = BaseEncoding.base64().decode(base64);

        byte[] uncompressed_binary;
        try (ByteArrayOutputStream uncompressed = new ByteArrayOutputStream()) {
            Inflater inflater = new Inflater();
            inflater.setInput(compressed_binary);
            while (!inflater.finished()) {
                byte[] buf = new byte[1024];
                int len = inflater.inflate(buf);
                uncompressed.write(buf, 0, len);
            }
            inflater.end();
            uncompressed_binary = uncompressed.toByteArray();
        } catch (IOException | DataFormatException ex) {
            ex.printStackTrace();
            return null;
        }

        List<ItemStack> ret = new ArrayList<>();
        try {
            int n = Byte.toUnsignedInt(uncompressed_binary[0]);
            int[] block_sizes = new int[n];
            for (int i = 0; i < n; i++) {
                block_sizes[i] = readInt(uncompressed_binary, 1 + 4 * i);
            }

            int offset = 1 + 4 * n;
            for (int i = 0; i < n; i++) {
                ret.add(itemFromBinary(uncompressed_binary, offset, block_sizes[i]));
                offset += block_sizes[i];
            }
        } catch (IOException | ReflectiveOperationException | IllegalArgumentException ex) {
            ex.printStackTrace();
            return null;
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
