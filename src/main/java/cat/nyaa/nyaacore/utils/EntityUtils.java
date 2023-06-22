package cat.nyaa.nyaacore.utils;


import net.minecraft.world.entity.EntityType;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityUtils {
    public static AtomicInteger FAKE_ENTITY_COUNTER = new AtomicInteger(0xffcc);

    private static Optional<EntityType<?>> getNmsEntityTypes(org.bukkit.entity.EntityType bukkitEntityType) {
        return EntityType.byString(bukkitEntityType.getKey().getKey());
    }

    public static int getUpdateInterval(org.bukkit.entity.EntityType bukkitEntityType) {
        return getNmsEntityTypes(bukkitEntityType).map(EntityType::updateInterval).orElse(3);
    }

    public static int getClientTrackingRange(org.bukkit.entity.EntityType bukkitEntityType) {
        return getNmsEntityTypes(bukkitEntityType).map(EntityType::clientTrackingRange).orElse(5);
    }

}
