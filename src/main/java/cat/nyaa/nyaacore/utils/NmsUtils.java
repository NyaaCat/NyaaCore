package cat.nyaa.nyaacore.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A collection of operations that cannot be done with NMS.
 * Downstream plugin authors can add methods here, so that
 * their plugins do not need to depend on NMS for just a
 * single function. It also makes upgrade a bit easier,
 * since all NMS codes are here.
 */
public final class NmsUtils {
    /* see CommandEntityData.java */
    public static void setEntityTag(Entity e, String tag) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) e).getHandle();

        if (nmsEntity instanceof Player) {
            throw new IllegalArgumentException("Player NBT cannot be edited");
        } else {
            CompoundTag nbtToBeMerged;

            try {
                nbtToBeMerged = TagParser.parseTag(tag);
            } catch (CommandSyntaxException ex) {
                throw new IllegalArgumentException("Invalid NBTTag string");
            }

            CompoundTag nmsOrigNBT = NbtPredicate.getEntityTagToCompare(nmsEntity); // entity to nbt
            CompoundTag nmsClonedNBT = nmsOrigNBT.copy(); // clone
            nmsClonedNBT.merge(nbtToBeMerged); // merge NBT
            if (nmsClonedNBT.equals(nmsOrigNBT)) {
            } else {
                UUID uuid = nmsEntity.getUUID(); // store UUID
                nmsEntity.load(nmsClonedNBT); // set nbt
                nmsEntity.setUUID(uuid); // set uuid
            }
        }
    }

    public static boolean createExplosion(World world, Entity entity, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return !((CraftWorld) world).getHandle().explode(((CraftEntity) entity).getHandle(), x, y, z, power, setFire, breakBlocks ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE).wasCanceled;
    }

    /**
     * fromMobSpawner is removed in 1.15.2 Spigot
     * use {Mob.isAware} instead.
     */
    @Deprecated
    public static boolean isFromMobSpawner(Entity entity) {
        return false;
    }

    /**
     * fromMobSpawner is removed in 1.15.2 Spigot
     * use {Mob.isAware} instead.
     */
    @Deprecated
    public static void setFromMobSpawner(Entity entity, boolean fromMobSpawner) {
//        if (entity instanceof CraftEntity) {
//            ((CraftEntity) entity).getHandle().fromMobSpawner = fromMobSpawner;
//        }
    }

    /**
     * Update the yaw &amp; pitch of entities. Can be used to set head orientation.
     *
     * @param entity   the living entity
     * @param newYaw   can be null if not to be modified
     * @param newPitch can be null if not to be modified
     */
    public static void updateEntityYawPitch(LivingEntity entity, Float newYaw, Float newPitch) {
        if (entity == null) throw new IllegalArgumentException();
        if (newYaw == null && newPitch == null) return;
        CraftLivingEntity nmsEntity = (CraftLivingEntity) entity;
        if (newYaw != null) {
            nmsEntity.getHandle().setYRot(newYaw);
        }

        if (newPitch != null) {
            nmsEntity.getHandle().setXRot(newPitch);
        }
    }

    /**
     * Set "OnGround" flag for an entity
     *
     * @param e          the entity
     * @param isOnGround new OnGround value
     */
    public static void setEntityOnGround(Entity e, boolean isOnGround) {
        if (e == null) throw new IllegalArgumentException();
        CraftEntity nmsEntity = (CraftEntity) e;
        nmsEntity.getHandle().setOnGround(isOnGround); //nms method renamed
    }

    public static List<Block> getTileEntities(World world) {
        Map<BlockPos, BlockEntity> BlockEntityList = ((CraftWorld) world).getHandle().capturedTileEntities;
        // Safe to parallelize getPosition and getBlockAt
        return BlockEntityList.entrySet().stream().parallel().map(Map.Entry::getKey).map(p -> world.getBlockAt(p.getX(), p.getY(), p.getZ())).collect(Collectors.toList());
    }

    public static List<BlockState> getBlockEntityBlockStates(World world) {
        Map<BlockPos, BlockEntity> BlockEntityList = ((CraftWorld) world).getHandle().capturedTileEntities;
        // Safe to parallelize getPosition and getBlockAt
        return BlockEntityList.entrySet().stream().parallel().map(Map.Entry::getKey).map(p -> world.getBlockAt(p.getX(), p.getY(), p.getZ())).map(Block::getState).collect(Collectors.toList());
    }
}
