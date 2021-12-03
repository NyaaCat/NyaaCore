package cat.nyaa.nyaacore.utils;

import net.minecraft.world.entity.projectile.EntityThrownTrident;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public final class TridentUtils {
    private static Field FIELD_DEALT_DAMAGE;

    static {
        try {
            var cls = EntityThrownTrident.class;
            FIELD_DEALT_DAMAGE = cls.getDeclaredField("as");
            FIELD_DEALT_DAMAGE.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static ItemStack getTridentItemStack(Trident entity) {
        return entity.getItem();
    }

    @Deprecated
    public static void setTridentItemStack(Trident entity, ItemStack itemStack) {
        entity.setItem(itemStack);
    }

    public static boolean getTridentDealtDamage(Trident entity) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            return (boolean) FIELD_DEALT_DAMAGE.get(thrownTrident);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setTridentDealtDamage(Trident entity, boolean dealtDamage) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            FIELD_DEALT_DAMAGE.set(thrownTrident, dealtDamage);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
