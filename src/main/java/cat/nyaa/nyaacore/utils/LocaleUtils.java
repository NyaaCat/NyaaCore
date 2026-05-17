package cat.nyaa.nyaacore.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * A wrapper for LangUtils
 */
public final class LocaleUtils {
    public static String getUnlocalizedName(Material material) {
        if (material == null) throw new IllegalArgumentException();
        return namespaceKeyToTranslationKey(material.isBlock() ? "block" : "item", material.getKey());
    }

    public static String getUnlocalizedName(ItemStack itemStack) {
        if (itemStack == null) throw new IllegalArgumentException();
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.getItem().getDescriptionId();
    }

    public static Component getNameComponent(ItemStack item) {
        if (item == null) throw new IllegalArgumentException();
        if (item.hasData(DataComponentTypes.CUSTOM_NAME)){
            return item.getData(DataComponentTypes.CUSTOM_NAME);
        }else{
            return Component.translatable(item);
        }
    }

    public static String getUnlocalizedName(Enchantment ench) {
        return namespaceKeyToTranslationKey("enchantment", ench.getKey());
    }

    public static Component getNameComponent(Enchantment ench) {
        return Component.translatable(ench);
    }

    public static String namespaceKeyToTranslationKey(String category, NamespacedKey namespacedKey) {
        return category + "." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }
}
