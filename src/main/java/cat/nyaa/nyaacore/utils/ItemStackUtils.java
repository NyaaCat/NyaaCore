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
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private static CraftWorld defaultWorld;

    private static CraftWorld getDefaultWorld() {
        if (defaultWorld == null) {
            var worlds = Bukkit.getWorlds();
            if (!worlds.isEmpty()) {
                var first = worlds.getFirst();
                if (first instanceof CraftWorld) {
                    defaultWorld = (CraftWorld) first;
                }
            }
            if(defaultWorld == null) {
                throw new IllegalStateException("No world available");
            }
        }
        return defaultWorld;
    }

    static {
        //noinspection deprecation
        currentDataVersion = Bukkit.getUnsafe().getDataVersion();
    }

    /**
     * Get the binary representation of ItemStack
     * for fast ItemStack serialization
     *
     * @param itemStack the item to be serialized
     * @return binary NBT representation of the item stack, or empty byte array for null/air items
     */
    public static byte[] itemToBinary(ItemStack itemStack) {
        // In Paper 1.21+, serializeAsBytes() throws IllegalArgumentException for empty items
        if (itemStack == null || itemStack.isEmpty()) {
            return new byte[0];
        }
        return itemStack.serializeAsBytes();
    }

    /**
     * Get the ItemStack from its binary representation
     * for fast ItemStack deserialization
     *
     * @param nbt binary item nbt data
     * @return constructed item
     */
    public static ItemStack itemFromBinary(byte[] nbt) throws IOException {
        if (nbt == null || nbt.length == 0) {
            return null;
        }
        // GZIP magic number check
        if (nbt.length < 2 || !(nbt[0] == (byte) 0x1f && nbt[1] == (byte) 0x8b)) {
            // Data is not in GZIP format, assume it's legacy data.
            // Legacy data was a "headless" NBT payload. We need to wrap it in a full
            // NBT structure, then GZIP compress it before deserialization.
            try (ByteArrayOutputStream fullNbtStream = new ByteArrayOutputStream();
                 DataOutputStream nbtDos = new DataOutputStream(fullNbtStream)) {
                
                // 1. Create a full NBT structure by adding a header
                nbtDos.writeByte(10); // TAG_Compound ID
                nbtDos.writeUTF("");  // Root tag name (empty)
                nbtDos.write(nbt);    // The original "headless" NBT payload
                nbtDos.flush();

                // 2. GZIP compress the full NBT structure
                try (ByteArrayOutputStream gzipStream = new ByteArrayOutputStream();
                     java.util.zip.GZIPOutputStream gzipOut = new java.util.zip.GZIPOutputStream(gzipStream)) {
                    gzipOut.write(fullNbtStream.toByteArray());
                    gzipOut.finish();
                    byte[] compressedNbt = gzipStream.toByteArray();
                    
                    // 3. Deserialize the GZIP-compressed data
                    return ItemStack.deserializeBytes(compressedNbt);
                }
            }
        } else {
            // Data is already in GZIP format (new format).
            return ItemStack.deserializeBytes(nbt);
        }
    }

    @Deprecated
    public static ItemStack itemFromBinary(byte[] nbt, int offset, int len) {
        return ItemStack.deserializeBytes(nbt);
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
        } catch (IOException ex) {
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
        return Bukkit.getUnsafe().serializeItemAsJson(itemStack).getAsString();
    }

    /**
     * @deprecated caller should use {@link CraftItemStack#asNMSCopy(ItemStack)} directly
     */
    @Deprecated
    public static Object asNMSCopy(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }
}
