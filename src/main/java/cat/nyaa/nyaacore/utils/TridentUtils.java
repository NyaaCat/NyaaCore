package cat.nyaa.nyaacore.utils;

import net.minecraft.server.v1_13_R2.EntityThrownTrident;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public final class TridentUtils {

    private static Class<?> entityThrownTrident = ReflectionUtils.getNMSClass("EntityThrownTrident");

    private static Field entityThrownTridentFieldAw;
    private static Field entityThrownTridentFieldAx;

    static {
        try {
            entityThrownTridentFieldAw = entityThrownTrident.getDeclaredField("aw");
            entityThrownTridentFieldAw.setAccessible(true);
            entityThrownTridentFieldAx = entityThrownTrident.getDeclaredField("ax");
            entityThrownTridentFieldAx.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack getTridentItemStack(Trident entity) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = (net.minecraft.server.v1_13_R2.ItemStack) entityThrownTridentFieldAw.get(thrownTrident);
            return CraftItemStack.asBukkitCopy(nmsItemStack);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setTridentItemStack(Trident entity, ItemStack itemStack) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            entityThrownTridentFieldAw.set(thrownTrident, nmsItemStack);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
