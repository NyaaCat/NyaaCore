package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * If one plugin register event here,
 * other plugins can emit that type of event
 * without directly depends on the plugin.
 */
public final class CustomEventUtils {
    private static final Map<String, Constructor<? extends Event>> eventMap = new HashMap<>();

    /**
     * Register an event and its constructor
     * @param name the event name, this is case-insensitive
     * @param eventConstructor the constructor
     */
    public static void registerEvent(String name, Constructor<? extends Event> eventConstructor) {
        name = name.toLowerCase();
        if (eventMap.containsKey(name)) {
            throw new IllegalArgumentException("duplicated event name");
        }
        eventMap.put(name , eventConstructor);
    }

    /**
     * emit a registered event.
     * @param name the event name
     * @param args constructor parameters
     */
    public static void emitEvent(String name, Object... args) {
        name = name.toLowerCase();
        Constructor<? extends Event> cons = eventMap.get(name);
        if (cons == null) {
            throw new UnsupportedOperationException("no such event type");
        }
        try {
            Event e = cons.newInstance(args);
            Bukkit.getPluginManager().callEvent(e);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
