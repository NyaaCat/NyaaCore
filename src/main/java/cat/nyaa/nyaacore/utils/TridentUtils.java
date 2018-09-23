package cat.nyaa.nyaacore.utils;

import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityThrownTrident;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TridentUtils {

    private static Class<?> entityThrownTrident = ReflectionUtils.getNMSClass("EntityThrownTrident");

    private static Field entityThrownTridentFieldAw;

    static {
        try {
            entityThrownTridentFieldAw = entityThrownTrident.getDeclaredField("aw");
            entityThrownTridentFieldAw.setAccessible(true);
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
}
