package cat.nyaa.nyaacore.utils;

import net.minecraft.world.entity.projectile.ThrownTrident;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public final class TridentUtils {
    @Deprecated
    public static ItemStack getTridentItemStack(Trident entity) {
        return entity.getItem();
    }

    @Deprecated
    public static void setTridentItemStack(Trident entity, ItemStack itemStack) {
        entity.setItem(itemStack);
    }

    public static boolean getTridentDealtDamage(Trident entity) {
        ThrownTrident thrownTrident = (ThrownTrident) ((CraftEntity) entity).getHandle();
        return (boolean) thrownTrident.dealtDamage;
    }

    public static void setTridentDealtDamage(Trident entity, boolean dealtDamage) {
        ThrownTrident thrownTrident = (ThrownTrident) ((CraftEntity) entity).getHandle();
        thrownTrident.dealtDamage = dealtDamage;
    }
}
