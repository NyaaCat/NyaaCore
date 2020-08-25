package cat.nyaa.nyaacore.configuration;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface Getter<T> {
    static <T extends ISerializable> Getter<T> from(T p, Class<? extends Getter<T>> cls) {
        return getAccessor(p, cls);
    }

    static <T, E> T getAccessor(E p, Class<? extends T> cls) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            try {
                return cls.getDeclaredConstructor(cls.getEnclosingClass()).newInstance(p);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * @param object Object to serialize to String
     * @return String representation of the object
     */
    String get(T object);
}
