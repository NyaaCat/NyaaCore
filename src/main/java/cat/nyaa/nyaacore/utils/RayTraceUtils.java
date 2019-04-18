package cat.nyaa.nyaacore.utils;

import net.minecraft.server.v1_13_R2.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class RayTraceUtils {
    public static Block rayTraceBlock(Player player) {
        float distance = player.getGameMode() == GameMode.CREATIVE ? 5.0F : 4.5F;
        Vector start = player.getEyeLocation().toVector();
        Vector end = start.clone().add(player.getEyeLocation().getDirection().multiply(distance));
        return rayTraceBlock(player.getWorld(), start, end, false, false, true);
    }

    public static Block rayTraceBlock(World world, Vector start, Vector end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        MovingObjectPosition mop = worldServer.rayTrace(toVec3DInternal(start), toVec3DInternal(end),
                stopOnLiquid ? FluidCollisionOption.ALWAYS : FluidCollisionOption.NEVER, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);

        if (mop != null) {
            BlockPosition blockPos = mop.getBlockPosition();
            return world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<LivingEntity> rayTraceEntites(Player player, float distance) {
        return rayTraceEntites(player, distance, not(player).and(canInteract()));
    }

    @SuppressWarnings("rawtypes")
    public static List<LivingEntity> rayTraceEntites(Player player, float distance, Predicate predicate) {
        Vector start = player.getEyeLocation().toVector();
        Vector end = start.clone().add(player.getEyeLocation().getDirection().multiply(distance));
        return rayTraceEntites(player.getWorld(), start, end, predicate);
    }

    public static List<LivingEntity> rayTraceEntites(World world, Vector start, Vector end) {
        return rayTraceEntites(world, start, end, canInteract());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<LivingEntity> rayTraceEntites(World world, Vector start, Vector end, Predicate predicate) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        List<EntityLiving> entityLivings = worldServer.a(EntityLiving.class, (Predicate<EntityLiving>) predicate);
        List<LivingEntity> result = new ArrayList<>();
        for (EntityLiving e : entityLivings) {
            AxisAlignedBB bb = e.getBoundingBox();
            MovingObjectPosition hit = bb.b(toVec3DInternal(start), toVec3DInternal(end));
            if (hit != null) {
                result.add((LivingEntity) e.getBukkitEntity());
            }
        }
        return result;
    }

    public static Object toVec3D(Vector v) {
        return toVec3DInternal(v);
    }

    private static Vec3D toVec3DInternal(Vector v) {
        return new Vec3D(v.getX(), v.getY(), v.getZ());
    }

    @SuppressWarnings("rawtypes")
    public static Predicate isAPlayer() {
        return (Object entity) -> entity instanceof EntityPlayer;
    }

    @SuppressWarnings("rawtypes")
    public static Predicate not(Entity e) {
        return (Object entity) ->
                !((EntityLiving) entity).getUniqueID().equals(e.getUniqueId());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Predicate canInteract() {
        return (Object input) -> {
            if (input instanceof EntityPlayer && ((EntityPlayer) input).isSpectator()) {
                return false;
            }
            return input != null && ((net.minecraft.server.v1_13_R2.Entity) input).isInteractable();//canBeCollidedWith
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Entity getTargetEntity(LivingEntity entity, float maxDistance) {
        EntityLiving nmsEntityLiving = ((CraftLivingEntity) entity).getHandle();
        net.minecraft.server.v1_13_R2.World world = nmsEntityLiving.world;
        Vec3D eyePos = nmsEntityLiving.i(1.0f);//getPositionEyes
        Vec3D start = nmsEntityLiving.f(1.0f);//getLook
        Vec3D end = eyePos.add(start.x * maxDistance, start.y * maxDistance, start.z * maxDistance);
        //getEntityBoundingBox().expand().expand()
        List<net.minecraft.server.v1_13_R2.Entity> entities = world.getEntities(nmsEntityLiving, nmsEntityLiving.getBoundingBox().b(start.x * maxDistance, start.y * maxDistance, start.z * maxDistance).grow(1.0D, 1.0D, 1.0D),
                (Predicate<? super net.minecraft.server.v1_13_R2.Entity>) canInteract()
        );
        net.minecraft.server.v1_13_R2.Entity targetEntity = null;
        double d2 = maxDistance;
        //Vec3D hitVec = null;
        for (net.minecraft.server.v1_13_R2.Entity entity1 : entities) {
            //getEntityBoundingBox().grow((double)entity1.getCollisionBorderSize());
            AxisAlignedBB axisAlignedBB = entity1.getBoundingBox().g((double) entity1.aM());
            MovingObjectPosition rayTraceResult = axisAlignedBB.b(eyePos, end);//calculateIntercept
            if (axisAlignedBB.b(eyePos)) {// contains
                if (d2 >= 0.0) {
                    targetEntity = entity1;
                    //hitVec = rayTraceResult == null ? eyePos : rayTraceResult.pos;
                    d2 = 0.0;
                }
            } else if (rayTraceResult != null) {
                double d3 = eyePos.f(rayTraceResult.pos);//distanceTo
                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getRootVehicle() == ((CraftEntity) entity).getHandle().getRootVehicle()) {//getLowestRidingEntity
                        if (d2 == 0.0D) {
                            targetEntity = entity1;
                            //hitVec = rayTraceResult.pos;
                        }
                    } else {
                        targetEntity = entity1;
                        //hitVec = rayTraceResult.pos;
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
