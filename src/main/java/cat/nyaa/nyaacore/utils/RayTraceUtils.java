package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class RayTraceUtils {
    public static Block rayTraceBlock(Player player) throws ReflectiveOperationException {
        float distance = player.getGameMode() == GameMode.CREATIVE ? 5.0F : 4.5F;
        Vector start = player.getEyeLocation().toVector();
        Vector end = start.clone().add(player.getEyeLocation().getDirection().multiply(distance));
        return rayTraceBlock(player.getWorld(), start, end, false, false, true);
    }

    public static Block rayTraceBlock(World world, Vector start, Vector end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) throws ReflectiveOperationException {
        Class<?> craftWorld = ReflectionUtils.getOBCClass("CraftWorld");
        Method getHandleMethod = ReflectionUtils.getMethod(craftWorld, "getHandle");
        Class<?> vec3D = ReflectionUtils.getNMSClass("Vec3D");
        Class<?> nmsWorld = ReflectionUtils.getNMSClass("World");
        Class<?> mopClass = ReflectionUtils.getNMSClass("MovingObjectPosition");
        Class<?> fcoClass = ReflectionUtils.getNMSClass("FluidCollisionOption");
        Method mop_getBlockPosMethod = ReflectionUtils.getMethod(mopClass, "a");
        Class<?> baseBlockPosition = ReflectionUtils.getNMSClass("BaseBlockPosition");
        Method bbp_getX = ReflectionUtils.getMethod(baseBlockPosition, "getX");
        Method bbp_getY = ReflectionUtils.getMethod(baseBlockPosition, "getY");
        Method bbp_getZ = ReflectionUtils.getMethod(baseBlockPosition, "getZ");
        Object worldServer = getHandleMethod.invoke(world);
        Method rayTraceMethod = ReflectionUtils.getMethod(nmsWorld, "rayTrace", vec3D, vec3D, fcoClass, boolean.class, boolean.class);
        Object mop = rayTraceMethod.invoke(worldServer, toVec3D(start), toVec3D(end),
                stopOnLiquid ? fcoClass.getEnumConstants()[2] : fcoClass.getEnumConstants()[0], ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
        if (mop != null) {
            Object blockPos = mop_getBlockPosMethod.invoke(mop);
            return world.getBlockAt((int) bbp_getX.invoke(blockPos), (int) bbp_getY.invoke(blockPos), (int) bbp_getZ.invoke(blockPos));
        }
        return null;
    }

    public static List<LivingEntity> rayTraceEntites(Player player, float distance) throws ReflectiveOperationException {
        return rayTraceEntites(player, distance, notPlayer(player));
    }

    @SuppressWarnings("rawtypes")
    public static List<LivingEntity> rayTraceEntites(Player player, float distance, Predicate predicate) throws ReflectiveOperationException {
        Vector start = player.getEyeLocation().toVector();
        Vector end = start.clone().add(player.getEyeLocation().getDirection().multiply(distance));
        return rayTraceEntites(player.getWorld(), start, end, predicate);
    }

    public static List<LivingEntity> rayTraceEntites(World world, Vector start, Vector end) throws ReflectiveOperationException {
        return rayTraceEntites(world, start, end, o -> true);
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    public static List<LivingEntity> rayTraceEntites(World world, Vector start, Vector end, Predicate predicate) throws ReflectiveOperationException {
        Class<?> craftWorld = ReflectionUtils.getOBCClass("CraftWorld");
        Method getHandleMethod = ReflectionUtils.getMethod(craftWorld, "getHandle");
        Class<?> vec3D = ReflectionUtils.getNMSClass("Vec3D");
        Class<?> nmsWorld = ReflectionUtils.getNMSClass("World");
        Object worldServer = getHandleMethod.invoke(world);
        Class<?> entityLiving = ReflectionUtils.getNMSClass("EntityLiving");
        Class<?> entity = ReflectionUtils.getNMSClass("Entity");
        Method getTargets = ReflectionUtils.getMethod(nmsWorld, "a", Class.class, Predicate.class);
        List<Object> entityLivings = (List<Object>) getTargets.invoke(worldServer, entityLiving, predicate);
        Method getBoundingBox = ReflectionUtils.getMethod(entity, "getBoundingBox");
        Class<?> axisAlignedBB = ReflectionUtils.getNMSClass("AxisAlignedBB");
        Method getHit = ReflectionUtils.getMethod(axisAlignedBB, "b", vec3D, vec3D);
        Method getUniqueID = ReflectionUtils.getMethod(entity, "getUniqueID");
        List<LivingEntity> result = new ArrayList<>();
        for (Object e : entityLivings) {
            Object bb = getBoundingBox.invoke(e);
            Object hit = getHit.invoke(bb, toVec3D(start), toVec3D(end));
            if (hit != null) {
                UUID uuid = (UUID) getUniqueID.invoke(e);
                result.add((LivingEntity) Bukkit.getServer().getEntity(uuid));
            }
        }
        return result;
    }

    public static Object toVec3D(Vector v) throws ReflectiveOperationException {
        Constructor<?> vec3d = ReflectionUtils.getNMSClass("Vec3D").getConstructor(double.class, double.class, double.class);
        return vec3d.newInstance(v.getX(), v.getY(), v.getZ());
    }

    @SuppressWarnings("rawtypes")
    public static Predicate isAPlayer() {
        return (Object entity) -> entity.getClass().getSimpleName().equals("EntityPlayer");
    }

    @SuppressWarnings("rawtypes")
    public static Predicate notPlayer(Player player) {
        return (Object entity) -> {
            Class<?> craftPlayer = ReflectionUtils.getOBCClass("entity.CraftPlayer");
            Method getHandleMethod = ReflectionUtils.getMethod(craftPlayer, "getHandle");
            try {
                Object entityPlayer = getHandleMethod.invoke(player);
                return !entity.equals(entityPlayer);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return false;
            }
        };
    }
}
