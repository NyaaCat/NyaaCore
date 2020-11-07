package cat.nyaa.nyaacore.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.getItem().f(nmsItemStack);
    }

    public static BaseComponent getNameComponent(ItemStack item) {
        if (item == null) throw new IllegalArgumentException();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return new TextComponent(item.getItemMeta().getDisplayName());
        if (item.getItemMeta() instanceof SkullMeta && ((SkullMeta) item.getItemMeta()).hasOwner()) {
            String key = getUnlocalizedName(item.getType()) + ".named";
            return new TranslatableComponent(key, ((SkullMeta) item.getItemMeta()).getOwningPlayer().getName());
        }
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        return new TranslatableComponent(nmsItemStack.getItem().f(nmsItemStack));
    }

    public static String getUnlocalizedName(Enchantment ench) {
        return namespaceKeyToTranslationKey("enchantment", ench.getKey());
    }

    public static BaseComponent getNameComponent(Enchantment ench) {
        return new TranslatableComponent(getUnlocalizedName(ench));
    }

    public static String namespaceKeyToTranslationKey(String category, NamespacedKey namespacedKey) {
        return category + "." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }
}
