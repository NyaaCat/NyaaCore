package cat.nyaa.nyaacore.utils;

import net.minecraft.core.IRegistry;
import net.minecraft.world.inventory.Containers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegistryUtils {
    public static int getMenuId(Object obj) {
        if (obj instanceof Containers<?>) {
            return IRegistry.MENU.getId((Containers<?>) obj);
        }
        throw new IllegalArgumentException("wrong parameter type");
    }

    public static Object getMenuTypeById(int id){
        return IRegistry.MENU.byId(id);
    }

    <T> int getId(@NotNull IRegistry<T> registry, @NotNull T object) {
        return registry.getId(object);
    }

    @Nullable <T> T byId(@NotNull IRegistry<T> registry, int id) {
        return registry.byId(id);
    }
}
