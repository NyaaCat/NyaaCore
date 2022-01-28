package cat.nyaa.nyaacore.utils;

import net.minecraft.core.IRegistry;
import net.minecraft.network.syncher.DataWatcherObject;
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

    /**
     * no cache
     *
     * @param entityClass         target class
     * @param EntityDataFieldName automatic obfuscation
     * @return entityDataIdOptional
     */
    public static Optional<Integer> getEntityDataId(Class<? extends net.minecraft.world.entity.Entity> entityClass, String EntityDataFieldName) {
        Field field = null;
        try {
            field = entityClass.getDeclaredField(EntityDataFieldName);
        } catch (NoSuchFieldException ignored) {
        }
        if (field == null) {
            Optional<String> fieldName = SpigotMappingUtils.getSimpleObfuscatedFieldNameOptional(entityClass.getName().replace('.', '/'), EntityDataFieldName, null);
            if (fieldName.isPresent()) {
                try {
                    field = entityClass.getDeclaredField(fieldName.get());
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
        if (field == null) return Optional.empty();
        field.trySetAccessible();
        try {
            return Optional.of(((DataWatcherObject<?>) field.get(null)).getId());
        } catch (IllegalAccessException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> getPlayerEntityDataId(String EntityDataFieldName) {
        return getEntityDataId(net.minecraft.world.entity.player.EntityHuman.class, EntityDataFieldName);
    }
}
