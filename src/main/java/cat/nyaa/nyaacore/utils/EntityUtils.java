package cat.nyaa.nyaacore.utils;


import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityUtils {
    public static AtomicInteger FAKE_ENTITY_COUNTER = new AtomicInteger(0xffcc);

    public static int getNewEntityId() {
        try {
            Field ENTITY_COUNTER = Entity.class.getDeclaredField("ENTITY_COUNTER");
            return ((AtomicInteger) ENTITY_COUNTER.get(null)).incrementAndGet();
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        return -FAKE_ENTITY_COUNTER.incrementAndGet();
    }

    private static Optional<EntityType<?>> getNmsEntityTypes(org.bukkit.entity.EntityType bukkitEntityType) {
        return EntityType.byString(bukkitEntityType.getKey().getKey());
    }

    public static Optional<Integer> getEntityTypeId(org.bukkit.entity.EntityType bukkitEntityType) {
        return getNmsEntityTypes(bukkitEntityType).map(Registry.ENTITY_TYPE::getId);
    }

    public static int getUpdateInterval(org.bukkit.entity.EntityType bukkitEntityType) {
        return getNmsEntityTypes(bukkitEntityType).map(EntityType::updateInterval).orElse(3);
    }

    public static int getClientTrackingRange(org.bukkit.entity.EntityType bukkitEntityType) {
        return getNmsEntityTypes(bukkitEntityType).map(EntityType::clientTrackingRange).orElse(5);
    }

}
