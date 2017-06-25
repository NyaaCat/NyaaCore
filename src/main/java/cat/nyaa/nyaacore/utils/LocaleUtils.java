package cat.nyaa.nyaacore.utils;

import com.meowj.langutils.lang.LanguageHelper;
import com.meowj.langutils.lang.convert.EnumItem;
import com.meowj.langutils.lang.convert.ItemEntry;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * A wrapper for LangUtils
 */
public final class LocaleUtils {
    public static String getUnlocalizedName(Material material) {
        if (material == null) throw new IllegalArgumentException();
        EnumItem enumItem = EnumItem.get(new ItemEntry(material));
        return enumItem != null ? enumItem.getUnlocalizedName() : material.toString();
    }

    public static String getUnlocalizedName(ItemStack itemStack) {
        if (itemStack == null) throw new IllegalArgumentException();
        return LanguageHelper.getItemUnlocalizedName(itemStack);
    }

    public static BaseComponent getNameComponent(ItemStack item) {
        if (item == null) throw new IllegalArgumentException();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return new TextComponent(item.getItemMeta().getDisplayName());
        Material type = item.getType();
        if (type == Material.SKULL_ITEM && item.getDurability() == 3) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta.hasOwner()) {
                return new TranslatableComponent("item.skull.player.name", meta.getOwner());
            } else {
                return new TranslatableComponent("item.skull.char.name");
            }
        } else {
            return new TranslatableComponent(getUnlocalizedName(item));
        }
    }

    public static String getUnlocalizedName(Enchantment ench) {
        return LanguageHelper.getEnchantmentUnlocalizedName(ench);
    }

    public static BaseComponent getNameComponent(Enchantment ench) {
        return new TranslatableComponent(getUnlocalizedName(ench));
    }
}
