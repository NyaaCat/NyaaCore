package cat.nyaa.nyaacore.component;

import java.util.HashMap;
import java.util.Map;

/**
 * Component APIs are designed to facilitate interoperability between NyaaCat plugins.
 * The Component interfaces defined here are generally taken from other NyaaCat plugins
 * so it could be meaningless for 3rd party plugins to implement.
 * <p>
 * This class is not thread safe.
 */
public final class NyaaComponent {
    private static final Map<Class<? extends IComponent>, IComponent> registeredComponents = new HashMap<>();

    public static <T extends IComponent> void register(Class<T> componentClass, T component) {
        if (component == null || componentClass == null)
            throw new IllegalArgumentException();
        if (!(componentClass.isInstance(component))) {
            throw new IllegalArgumentException();
        }
        if (!registeredComponents.containsKey(componentClass)) {
            registeredComponents.put(componentClass, component);
        } else {
            IComponent old_component = registeredComponents.get(componentClass);
            if (old_component.canReplaceMe(component)) {
                registeredComponents.put(componentClass, component);
            } else {
                throw new ComponentDuplicatedException(componentClass, old_component, component);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends IComponent> T get(Class<T> componentClass) {
        if (componentClass == null) throw new IllegalArgumentException();
        if (!registeredComponents.containsKey(componentClass))
            throw new ComponentNotAvailableException(componentClass);
        return (T) registeredComponents.get(componentClass);
    }
}
