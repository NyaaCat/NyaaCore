package cat.nyaa.nyaacore.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public final class ItemStackUtils {
    private static final String NYAACORE_ITEMSTACK_DATAVERSION_KEY = "nyaacore_itemstack_dataversion";
    private static final int NYAACORE_ITEMSTACK_DEFAULT_DATAVERSION = 1139;
    private static final ThreadLocal<Inflater> NYAA_INFLATER = ThreadLocal.withInitial(Inflater::new);
    private static final ThreadLocal<Deflater> NYAA_DEFLATER = ThreadLocal.withInitial(Deflater::new);
    private static final int currentDataVersion;
    private static final Cache<String, List<ItemStack>> itemDeserializerCache = CacheBuilder.newBuilder()
            .weigher((String k, List<ItemStack> v) -> k.getBytes().length)
            .maximumWeight(256L * 1024 * 1024).build(); // Hard Coded 256M
    private static NbtAccounter unlimitedNbtAccounter = null;

    static {
        //noinspection deprecation
        currentDataVersion = Bukkit.getUnsafe().getDataVersion();
    }

    /**
     * Get the binary representation of ItemStack
     * for fast ItemStack serialization
     *
     * @param itemStack the item to be serialized
     * @return binary NBT representation of the item stack
     */
    public static byte[] itemToBinary(ItemStack itemStack) throws IOException {
        net.minecraft.world.item.ItemStack nativeItemStack = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag CompoundTag = new CompoundTag();
        nativeItemStack.save(CompoundTag);
        CompoundTag.putInt(NYAACORE_ITEMSTACK_DATAVERSION_KEY, currentDataVersion);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        CompoundTag.write(dos);
        byte[] outputByteArray = baos.toByteArray();
        dos.close();
        baos.close();
        return outputByteArray;
    }

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

    public static ItemStack itemFromBinary(byte[] nbt, int offset, int len) throws IOException {
        if (unlimitedNbtAccounter == null) {
            unlimitedNbtAccounter = NbtAccounter.UNLIMITED;
        }

        //Constructor<?> constructNativeItemStackFromCompoundTag = classNativeItemStack.getConstructor(classCompoundTag);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nbt, offset, len);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        CompoundTag reconstructedCompoundTag = CompoundTag.TYPE.load(dataInputStream, 0, unlimitedNbtAccounter);
        dataInputStream.close();
        byteArrayInputStream.close();
        int dataVersion = reconstructedCompoundTag.getInt(NYAACORE_ITEMSTACK_DATAVERSION_KEY);
        if (dataVersion > 0) {
            reconstructedCompoundTag.remove(NYAACORE_ITEMSTACK_DATAVERSION_KEY);
        }
        if (dataVersion < currentDataVersion) {
            // 1.12 to 1.13
            if (dataVersion <= 0) {
                dataVersion = NYAACORE_ITEMSTACK_DEFAULT_DATAVERSION;
            }
            DSL.TypeReference References_ITEM_STACK = References.ITEM_STACK;
            NbtOps NbtOps_instance = NbtOps.INSTANCE;
            DataFixer dataFixer_instance = DataFixers.getDataFixer();
            Dynamic<Tag> dynamicInstance = new Dynamic<>(NbtOps_instance, reconstructedCompoundTag);
            Dynamic<Tag> out = dataFixer_instance.update(References_ITEM_STACK, dynamicInstance, dataVersion, currentDataVersion);
            reconstructedCompoundTag = (CompoundTag) out.getValue();
        }
        net.minecraft.world.item.ItemStack reconstructedNativeItemStack = net.minecraft.world.item.ItemStack.of(reconstructedCompoundTag);
        return CraftItemStack.asBukkitCopy(reconstructedNativeItemStack);
    }

    private static byte[] compress(byte[] data) {
        byte[] ret;
        Deflater deflater = NYAA_DEFLATER.get();
        deflater.reset();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteStreams.copy(new DeflaterInputStream(bis, deflater), bos);
            ret = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }

    private static byte[] decompress(byte[] data) {
        byte[] ret;
        Inflater inflater = NYAA_INFLATER.get();
        inflater.reset();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteStreams.copy(new InflaterInputStream(bis, inflater), bos);
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
        if (items.isEmpty()) return "";
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
        List<ItemStack> stack = itemDeserializerCache.getIfPresent(base64);
        if (stack != null) return stack.stream().map(ItemStack::clone).collect(Collectors.toList());
        if (base64.length() <= 0) return new ArrayList<>();

        byte[] uncompressedBinary = decompress(BaseEncoding.base64().decode(base64));
        List<ItemStack> ret = new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(uncompressedBinary);
             DataInputStream dis = new DataInputStream(bis)) {
            int n = dis.readByte();
            int[] nbtLength = new int[n];
            for (int i = 0; i < n; i++) nbtLength[i] = dis.readInt();
            for (int i = 0; i < n; i++) {
                byte[] tmp = new byte[nbtLength[i]];
                dis.readFully(tmp);
                ret.add(itemFromBinary(tmp));
            }
        } catch (IOException | ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        itemDeserializerCache.put(base64, ret.stream().map(ItemStack::clone).collect(Collectors.toList()));
        return ret;
    }

    public static String itemToBase64(ItemStack item) {
        if (item == null) throw new IllegalArgumentException();
        return itemsToBase64(Collections.singletonList(item));
    }

    public static ItemStack itemFromBase64(String base64) {
        if (base64 == null) throw new IllegalArgumentException();
        List<ItemStack> ret = itemsFromBase64(base64);
        if (ret != null && !ret.isEmpty()) return ret.get(0);
        return null;
    }

    /**
     * <a href="https://github.com/sainttx/Auctions/blob/12533c9af0b1dba700473bf728895abb9ff5b33b/Auctions/src/main/java/com/sainttx/auctions/SimpleMessageFactory.java#L197">...</a>
     * Convert an item to its JSON representation to be shown in chat.
     * NOTE: this method has no corresponding deserializer.
     */
    public static String itemToJson(ItemStack itemStack) throws RuntimeException {
        CompoundTag nmsCompoundTagObj; // This will just be an empty CompoundTag instance to invoke the saveNms method
        net.minecraft.world.item.ItemStack nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        CompoundTag itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

        try {
            nmsCompoundTagObj = new CompoundTag();
            nmsItemStackObj = CraftItemStack.asNMSCopy(itemStack);
            itemAsJsonObject = nmsItemStackObj.save(nmsCompoundTagObj);
        } catch (Throwable t) {
            throw new RuntimeException("failed to serialize itemstack to nms item", t);
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }

    /**
     * @deprecated caller should use {@link CraftItemStack#asNMSCopy(ItemStack)} directly
     */
    @Deprecated
    public static Object asNMSCopy(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }
}
