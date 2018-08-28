package cat.nyaa.nyaacore.utils;

import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TridentUtils {
    public static ItemStack getTridentItemStack(Trident entity) {
        try {
            Class<?> craftEntity = ReflectionUtils.getOBCClass("entity.CraftEntity");
            Class<?> entityThrownTrident = ReflectionUtils.getNMSClass("EntityThrownTrident");
            Field craftEntityFieldEntity = craftEntity.getDeclaredField("entity");
            craftEntityFieldEntity.setAccessible(true);
            Field entityThrownTridentFieldH = entityThrownTrident.getDeclaredField("aw");
            entityThrownTridentFieldH.setAccessible(true);
            Object thrownTrident = craftEntityFieldEntity.get(entity);
            Object nmsItemStack = entityThrownTridentFieldH.get(thrownTrident);
            Class<?> craftItemStack = ReflectionUtils.getOBCClass("inventory.CraftItemStack");
            Method asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsItemStack.getClass());
            return (ItemStack) asBukkitCopy.invoke(null, nmsItemStack);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setTridentItemStack(Trident entity, ItemStack itemStack) {
        try {
            Class<?> craftEntity = ReflectionUtils.getOBCClass("entity.CraftEntity");
            Class<?> entityThrownTrident = ReflectionUtils.getNMSClass("EntityThrownTrident");
            Field craftEntityFieldEntity = craftEntity.getDeclaredField("entity");
            craftEntityFieldEntity.setAccessible(true);
            Field entityThrownTridentFieldH = entityThrownTrident.getDeclaredField("aw");
            entityThrownTridentFieldH.setAccessible(true);
            Object thrownTrident = craftEntityFieldEntity.get(entity);
            Class<?> craftItemStack = ReflectionUtils.getOBCClass("inventory.CraftItemStack");
            Method asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            Object nmsFakeItem = asNMSCopy.invoke(null, itemStack);
            entityThrownTridentFieldH.set(thrownTrident, nmsFakeItem);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
