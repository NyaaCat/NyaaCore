package cat.nyaa.nyaacore.utils;

import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityUtils {
    public static AtomicInteger FAKE_ENTITY_COUNTER = new AtomicInteger(0xffcc);

    public static int getNewEntityId() {
        try {
            Field ENTITY_COUNTER = Entity.class.getDeclaredField("ENTITY_COUNTER");
            return ((AtomicInteger) ENTITY_COUNTER.get(null)).incrementAndGet();
        } catch (IllegalAccessException | NoSuchFieldException ignored) {}
        return -FAKE_ENTITY_COUNTER.incrementAndGet();
    }
}
