package cat.nyaa.nyaacore.configuration;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Store NBT-represented ItemStacks in config file, rather than default yaml-representation.
 */
public class NbtItemStack implements ConfigurationSerializable {
    public ItemStack it;

    public NbtItemStack(ItemStack it) {
        this.it = it;
    }

    public static NbtItemStack deserialize(Map<String, Object> map) {
        try {
            String nbt = (String) map.getOrDefault("nbt", 0);
            if (nbt == null || "<null>".equalsIgnoreCase(nbt)) return new NbtItemStack(null);
            return new NbtItemStack(ItemStackUtils.itemFromBase64(nbt));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new NbtItemStack(null);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        if (it != null) {
            try {
                ret.put("nbt", ItemStackUtils.itemToBase64(it));
            } catch (Exception ex) {
                ex.printStackTrace();
                ret.put("nbt", "<null>");
            }
        } else {
            ret.put("nbt", "<null>");
        }

        return ret;
    }
}