package cat.nyaa.nyaacore.utils;

import net.minecraft.core.IRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import org.bukkit.entity.EntityType;

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

    public static Optional<Integer> getEntityTypeId(EntityType entityType) {
        Optional<EntityTypes<?>> entityTypesOptional = EntityTypes.byString(entityType.getKey().getKey());
        return entityTypesOptional.map(IRegistry.ENTITY_TYPE::getId);
    }
}
