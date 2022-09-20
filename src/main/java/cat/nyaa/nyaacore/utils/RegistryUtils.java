package cat.nyaa.nyaacore.utils;

import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegistryUtils {
    public static int getMenuId(Object obj) {
        if (obj instanceof MenuType<?>) {
            return Registry.MENU.getId((MenuType<?>) obj);
        }
        throw new IllegalArgumentException("wrong parameter type");
    }

    public static Object getMenuTypeById(int id) {
        return Registry.MENU.byId(id);
    }

    <T> int getId(@NotNull Registry<T> registry, @NotNull T object) {
        return registry.getId(object);
    }

    @Nullable <T> T byId(@NotNull Registry<T> registry, int id) {
        return registry.byId(id);
    }
}
