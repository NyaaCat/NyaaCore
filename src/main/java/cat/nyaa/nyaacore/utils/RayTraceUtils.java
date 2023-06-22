package cat.nyaa.nyaacore.utils;

import net.minecraft.world.phys.Vec3;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RayTraceUtils {
    public static Block rayTraceBlock(Player player) {
        float distance = player.getGameMode() == GameMode.CREATIVE ? 5.0F : 4.5F;
        RayTraceResult r = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), distance, FluidCollisionMode.NEVER, false);
        if (r != null) {
            return r.getHitBlock();
        }
        return null;
    }

    public static List<LivingEntity> rayTraceEntities(Player player, float distance) {
        return rayTraceEntities(player, distance, not(player).and(canInteract()));
    }

    public static List<LivingEntity> rayTraceEntities(LivingEntity player, float distance, Predicate<Entity> predicate) {
        List<LivingEntity> result = new ArrayList<>();
        Vector start = player.getEyeLocation().toVector();
        Vector end = start.clone().add(player.getEyeLocation().getDirection().multiply(distance));
        for (Entity e : player.getWorld().getNearbyEntities(player.getEyeLocation(), distance, distance, distance, predicate)) {
            if (e instanceof LivingEntity && e instanceof CraftEntity && e.isValid()) {
                net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) e).getHandle();
                Optional<Vec3> hit = nmsEntity.getBoundingBox().clip(toVec3DInternal(start), toVec3DInternal(end));
                if (hit.isPresent()) {
                    result.add((LivingEntity) e);
                }
            }
        }
        return result;
    }

    public static Object toVec3D(Vector v) {
        return toVec3DInternal(v);
    }

    private static Vec3 toVec3DInternal(Vector v) {
        return new Vec3(v.getX(), v.getY(), v.getZ());
    }

    public static Predicate<Entity> isAPlayer() {
        return entity -> entity instanceof Player;
    }

    public static Predicate<Entity> not(Entity e) {
        return entity -> !entity.getUniqueId().equals(e.getUniqueId());
    }

    public static Predicate<Entity> canInteract() {
        return input -> {
            if (input instanceof Player && ((Player) input).getGameMode() == GameMode.SPECTATOR) {
                return false;
            }
            return input instanceof LivingEntity && ((LivingEntity) input).isCollidable();
        };
    }

    public static Entity getTargetEntity(Player p) {
        return getTargetEntity(p, getDistanceToBlock(p, p.getGameMode() == GameMode.CREATIVE ? 6.0F : 4.5F));
    }

    public static Entity getTargetEntity(LivingEntity p, float maxDistance, boolean ignoreBlocks) {
        if (!ignoreBlocks) {
            maxDistance = getDistanceToBlock(p, maxDistance);
        }
        return getTargetEntity(p, maxDistance);
    }

    public static float getDistanceToBlock(LivingEntity entity, float maxDistance) {
        RayTraceResult r = entity.getWorld().rayTraceBlocks(entity.getEyeLocation(), entity.getEyeLocation().getDirection(), maxDistance);
        if (r != null) {
            return (float) entity.getEyeLocation().distance(r.getHitPosition().toLocation(entity.getWorld()));
        }
        return maxDistance;
    }

    public static Entity getTargetEntity(LivingEntity entity, float maxDistance) {
        RayTraceResult r = entity.getWorld().rayTraceEntities(entity.getEyeLocation(), entity.getEyeLocation().getDirection(), maxDistance,
                e -> e != null &&
                        (e instanceof LivingEntity || e.getType() == EntityType.ITEM_FRAME || e.getType() == EntityType.GLOW_ITEM_FRAME) &&
                        !(e instanceof LivingEntity && !((LivingEntity) e).isCollidable()) &&
                        e.getUniqueId() != entity.getUniqueId() &&
                        !(e instanceof Player && ((Player) e).getGameMode() == GameMode.SPECTATOR));
        if (r != null) {
            return r.getHitEntity();
        }
        return null;
    }
}
