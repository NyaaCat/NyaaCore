package cat.nyaa.nyaacore.utils;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RayTraceUtils {
    public static Block rayTraceBlock(Player player) throws ReflectiveOperationException {
        float distance = player.getGameMode() == GameMode.CREATIVE ? 5.0F : 4.5F;
        Vector start = player.getEyeLocation().toVector();
        Vector end = start.clone().add(player.getEyeLocation().getDirection().multiply(distance));
        return rayTraceBlock(player.getWorld(), start, end, false, false, true);
    }

    public static Block rayTraceBlock(World world, Vector start, Vector end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) throws ReflectiveOperationException {
        Class craftWorld = ReflectionUtils.getOBCClass("CraftWorld");
        Method getHandleMethod = ReflectionUtils.getMethod(craftWorld, "getHandle");
        Class<?> vec3D = ReflectionUtils.getNMSClass("Vec3D");
        Class<?> nmsWorld = ReflectionUtils.getNMSClass("World");
        Class<?> mopClass = ReflectionUtils.getNMSClass("MovingObjectPosition");
        Method mop_getBlockPosMethod = ReflectionUtils.getMethod(mopClass, "a");
        Class<?> baseBlockPosition = ReflectionUtils.getNMSClass("BaseBlockPosition");
        Method bbp_getX = ReflectionUtils.getMethod(baseBlockPosition, "getX");
        Method bbp_getY = ReflectionUtils.getMethod(baseBlockPosition, "getY");
        Method bbp_getZ = ReflectionUtils.getMethod(baseBlockPosition, "getZ");
        Object worldServer = getHandleMethod.invoke(world);
        Method rayTraceMethod = ReflectionUtils.getMethod(nmsWorld, "rayTrace", vec3D, vec3D, boolean.class, boolean.class, boolean.class);
        Object mop = rayTraceMethod.invoke(worldServer, toVec3D(start), toVec3D(end),
                stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
        if (mop != null) {
            Object blockPos = mop_getBlockPosMethod.invoke(mop);
            return world.getBlockAt((int) bbp_getX.invoke(blockPos), (int) bbp_getY.invoke(blockPos), (int) bbp_getZ.invoke(blockPos));
        }
        return null;
    }

    public static Object toVec3D(Vector v) throws ReflectiveOperationException {
        Constructor<?> vec3d = ReflectionUtils.getNMSClass("Vec3D").getConstructor(double.class, double.class, double.class);
        return vec3d.newInstance(v.getX(), v.getY(), v.getZ());
    }
}
