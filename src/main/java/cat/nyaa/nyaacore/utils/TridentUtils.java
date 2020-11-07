package cat.nyaa.nyaacore.utils;

import net.minecraft.server.v1_16_R3.EntityThrownTrident;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public final class TridentUtils {

    private static final Class<?> entityThrownTrident = ReflectionUtils.getNMSClass("EntityThrownTrident");

    private static Field entityThrownTridentFieldAx;

    static {
        try {
            entityThrownTridentFieldAx = entityThrownTrident.getDeclaredField("ax");
            entityThrownTridentFieldAx.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack getTridentItemStack(Trident entity) {
        EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = thrownTrident.trident;
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    public static void setTridentItemStack(Trident entity, ItemStack itemStack) {
        EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
        thrownTrident.trident = CraftItemStack.asNMSCopy(itemStack);
    }

    public static boolean getTridentDealtDamage(Trident entity) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            return (boolean) entityThrownTridentFieldAx.get(thrownTrident);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setTridentDealtDamage(Trident entity, boolean dealtDamage) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            entityThrownTridentFieldAx.set(thrownTrident, dealtDamage);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
