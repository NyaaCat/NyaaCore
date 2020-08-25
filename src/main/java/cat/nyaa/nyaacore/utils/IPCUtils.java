package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Inter-Plugin Communication Utils
 * <p>
 * If one plugin register events or methods here,
 * other plugins can emit the event or call the method
 * without directly depends on the plugin.
 *
 * @deprecated magic strings and magic argument list
 */
@Deprecated
public final class IPCUtils {
    private static final Map<String, Constructor<? extends Event>> eventMap = new HashMap<>();
    private static final Map<String, Method> methodMap = new HashMap<>();

    /**
     * Register an event and its constructor
     *
     * @param name             the event name, this is case-insensitive
     * @param eventConstructor the constructor
     */
    public static void registerEvent(String name, Constructor<? extends Event> eventConstructor) {
        name = name.toLowerCase();
        if (eventMap.containsKey(name)) {
            throw new IllegalArgumentException("duplicated event name");
        }
        eventMap.put(name, eventConstructor);
    }

    /**
     * emit a registered event.
     *
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

    /**
     * Register a method, the method must be static
     */
    public static void registerMethod(String name, Method method) {
        name = name.toLowerCase();
        if (methodMap.containsKey(name)) {
            throw new IllegalArgumentException("duplicated method name");
        }
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("method not static");
        }
        methodMap.put(name, method);
    }

    /**
     * Call the method with the given args.
     *
     * @param name method name, case-insensitive
     * @param args method arguments
     * @return method return value.
     */
    public static Object callMethod(String name, Object... args) {
        name = name.toLowerCase();
        Method m = methodMap.get(name);
        if (m == null) {
            throw new UnsupportedOperationException("no such method");
        }
        try {
            return m.invoke(null, args);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
