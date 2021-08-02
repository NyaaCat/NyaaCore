package cat.nyaa.nyaacore.utils;

import net.minecraft.world.entity.projectile.EntityThrownTrident;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public final class TridentUtils {

    private static final Class<?> entityThrownTrident = net.minecraft.world.entity.projectile.EntityThrownTrident.class;

    private static Field entityThrownTridentFieldDealtDamage;

    static {
        try {
            entityThrownTridentFieldDealtDamage = entityThrownTrident.getDeclaredField("ar");//1.17&1.17.1 = ar,1.16.3 = ai(not support)
            entityThrownTridentFieldDealtDamage.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack getTridentItemStack(Trident entity) {
        EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
        // net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = thrownTrident.trident;
        net.minecraft.world.item.ItemStack nmsItemStack = thrownTrident.aq;
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    public static void setTridentItemStack(Trident entity, ItemStack itemStack) {
        EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
        // thrownTrident.trident = CraftItemStack.asNMSCopy(itemStack);
        thrownTrident.aq = CraftItemStack.asNMSCopy(itemStack);
    }

    public static boolean getTridentDealtDamage(Trident entity) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            return (boolean) entityThrownTridentFieldDealtDamage.get(thrownTrident);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setTridentDealtDamage(Trident entity, boolean dealtDamage) {
        try {
            EntityThrownTrident thrownTrident = (EntityThrownTrident) ((CraftEntity) entity).getHandle();
            entityThrownTridentFieldDealtDamage.set(thrownTrident, dealtDamage);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
