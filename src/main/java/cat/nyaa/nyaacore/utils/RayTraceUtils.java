package cat.nyaa.nyaacore.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public static Object toVec3D(Vector v) {
        return new Vec3D(v.getX(), v.getY(), v.getZ());
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

    public static Entity getTargetEntity(Player p) {
        Vector start = p.getEyeLocation().toVector();
        Vector end = start.clone().add(p.getEyeLocation().getDirection().multiply(p.getGameMode() == GameMode.CREATIVE ? 6.0F : 4.5F));
        return getTargetEntity(p, getDistanceToBlock(p.getWorld(), start, end, false, false, true));
    }

    public static Entity getTargetEntity(LivingEntity p, float maxDistance, boolean ignoreBlocks) {
        Vector start = p.getEyeLocation().toVector();
        Vector end = start.clone().add(p.getEyeLocation().getDirection().multiply(maxDistance));
        if (!ignoreBlocks) {
            maxDistance = getDistanceToBlock(p.getWorld(), start, end, false, false, true);
        }
        return getTargetEntity(p, maxDistance);
    }

    public static float getDistanceToBlock(World world, Vector start, Vector end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        MovingObjectPosition mop = worldServer.rayTrace((Vec3D) toVec3D(start), (Vec3D) toVec3D(end), stopOnLiquid ? FluidCollisionOption.ALWAYS : FluidCollisionOption.NEVER, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
        if (mop != null && mop.type == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return (float) mop.pos.f((Vec3D) toVec3D(start));
        }
        return (float) start.distance(end);
    }

    public static Entity getTargetEntity(LivingEntity entity, float maxDistance) {
        EntityLiving nmsEntityLiving = ((CraftLivingEntity) entity).getHandle();
        net.minecraft.server.v1_13_R2.World world = nmsEntityLiving.world;
        Vec3D eyePos = nmsEntityLiving.i(1.0f);//getPositionEyes
        Vec3D start = nmsEntityLiving.f(1.0f);//getLook
        Vec3D end = eyePos.add(start.x * maxDistance, start.y * maxDistance, start.z * maxDistance);
        //getEntityBoundingBox().expand().expand()
        List<net.minecraft.server.v1_13_R2.Entity> entities = world.getEntities(nmsEntityLiving, nmsEntityLiving.getBoundingBox().b(start.x * maxDistance, start.y * maxDistance, start.z * maxDistance).grow(1.0D, 1.0D, 1.0D), Predicates.and(new Predicate<net.minecraft.server.v1_13_R2.Entity>() {
            @Override
            public boolean apply(@Nullable net.minecraft.server.v1_13_R2.Entity input) {
                if (input instanceof EntityPlayer && ((EntityPlayer) input).isSpectator()) {
                    return false;
                }
                return input != null && input.isInteractable();//canBeCollidedWith
            }
        }));
        net.minecraft.server.v1_13_R2.Entity targetEntity = null;
        double d2 = maxDistance;
        Vec3D hitVec = null;
        for (net.minecraft.server.v1_13_R2.Entity entity1 : entities) {
            //getEntityBoundingBox().grow((double)entity1.getCollisionBorderSize());
            AxisAlignedBB axisAlignedBB = entity1.getBoundingBox().g((double) entity1.aM());
            MovingObjectPosition rayTraceResult = axisAlignedBB.b(eyePos, end);//calculateIntercept
            if (axisAlignedBB.b(eyePos)) {// contains
                if (d2 >= 0.0) {
                    targetEntity = entity1;
                    hitVec = rayTraceResult == null ? eyePos : rayTraceResult.pos;
                    d2 = 0.0;
                }
            } else if (rayTraceResult != null) {
                double d3 = eyePos.f(rayTraceResult.pos);//distanceTo
                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getRootVehicle() == ((CraftEntity) entity).getHandle().getRootVehicle()) {//getLowestRidingEntity
                        if (d2 == 0.0D) {
                            targetEntity = entity1;
                            hitVec = rayTraceResult.pos;
                        }
                    } else {
                        targetEntity = entity1;
                        hitVec = rayTraceResult.pos;
                        d2 = d3;
                    }
                }
            }
        }
        //EntityLivingBase
        if (targetEntity instanceof EntityLiving || targetEntity instanceof EntityItemFrame) {
            return targetEntity.getBukkitEntity();
        }
        return null;
    }
}
