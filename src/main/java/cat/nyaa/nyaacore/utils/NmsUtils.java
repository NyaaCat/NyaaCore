package cat.nyaa.nyaacore.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_13_R2.CriterionConditionNBT;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.MojangsonParser;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

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
}
