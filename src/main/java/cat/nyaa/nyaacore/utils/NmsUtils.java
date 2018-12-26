package cat.nyaa.nyaacore.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_13_R2.CriterionConditionNBT;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.MojangsonParser;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public final class NmsUtils {
    /* see CommandEntityData.java */
    public static void setEntityTag(Entity e, String tag) {
        net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) e).getHandle();

        if (nmsEntity instanceof EntityHuman) {
            throw new IllegalArgumentException("Player NBT cannot be edited");
        } else {
            NBTTagCompound nbtToBeMerged;

            try {
                nbtToBeMerged = MojangsonParser.parse(tag);
            } catch (CommandSyntaxException ex) {
                throw new IllegalArgumentException("Invalid NBTTag string");
            }

            NBTTagCompound nmsOrigNBT = CriterionConditionNBT.b(nmsEntity); // entity to nbt
            NBTTagCompound nmsClonedNBT = nmsOrigNBT.clone(); // clone
            nmsClonedNBT.a(nbtToBeMerged); // merge NBT
            if (nmsClonedNBT.equals(nmsOrigNBT)) {
                return;
            } else {
                UUID uuid = nmsEntity.getUniqueID(); // store UUID
                nmsEntity.f(nmsClonedNBT); // set nbt
                nmsEntity.a(uuid); // set uuid
            }
        }
    }

    public static boolean createExplosion(World world, Entity entity, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return !((CraftWorld) world).getHandle().createExplosion(((CraftEntity) entity).getHandle(), x, y, z, power, setFire, breakBlocks).wasCanceled;
    }

    public static boolean isFromMobSpawner(Entity entity) {
        return entity instanceof CraftEntity && ((CraftEntity) entity).getHandle().fromMobSpawner;
    }

    public static void setFromMobSpawner(Entity entity, boolean fromMobSpawner) {
        if (entity instanceof CraftEntity) {
            ((CraftEntity) entity).getHandle().fromMobSpawner = fromMobSpawner;
        }
    }

    /**
     * Update the yaw & pitch of entities. Can be used to set head orientation.
     *
     * @param entity the living entity
     * @param newYaw can be null if not to be modified
     * @param newPitch can be null if not to be modified
     */
    public static void updateEntityYawPitch(LivingEntity entity, Float newYaw, Float newPitch) {
        if (entity == null) throw new IllegalArgumentException();
        if (newYaw == null && newPitch == null) return;
        CraftLivingEntity nmsEntity = (CraftLivingEntity) entity;
        if (newYaw != null) {
            nmsEntity.getHandle().yaw = newYaw;
            nmsEntity.getHandle().lastYaw = newYaw;
            nmsEntity.getHandle().setHeadRotation(newYaw);
        }

        if (newPitch != null) {
            nmsEntity.getHandle().a();
            nmsEntity.getHandle().pitch = newPitch;
            nmsEntity.getHandle().lastPitch = newPitch;
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
        nmsEntity.getHandle().onGround = isOnGround;
    }
}
