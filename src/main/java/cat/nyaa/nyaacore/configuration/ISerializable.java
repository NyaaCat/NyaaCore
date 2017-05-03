package cat.nyaa.nyaacore.configuration;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Serialize or Deserialize objects into ConfigurationSection
 */
public interface ISerializable {
    /**
     * For informative only
     */
    @Target(ElementType.FIELD)
    public @interface Ephemeral {
    }

    /**
     * Indicates a field can be serialized.
     * These four types are supported
     * 1. Primitive types supported by ConfigurationSection (should put() & get() directly)
     * 2. ISerializable
     * //3. List<Type1/2/3/4> (not implemented)
     * 4. Map<String, Type1/2/3/4>
     * NOTE:
     * ENUM without typehint is not supported (ENUM in Map or List) They will be deserialized as string
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Serializable {
        /**
         * @return the name used in yaml config
         */
        String name() default "";

        /**
         * deserializer will check all the alias, but only name will be used for serialization
         * Usually used for backward compatibility
         *
         * @return list of aliases
         */
        String[] alias() default {};

        /**
         * When set to true, this field is ignored
         */
        boolean manualSerialization() default false;
    }

    /**
     * Must be placed on a {@link FileConfigure} field
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StandaloneConfig {
        /**
         * When set to true, this field is ignored
         */
        boolean manualSerialization() default false;
    }

    default void deserialize(ConfigurationSection config) {
        deserialize(config, this);
    }

    static void deserialize(ConfigurationSection config, ISerializable serializable) {
        popSerializable(popPrimitiveMap(config), serializable);
    }

    default void serialize(ConfigurationSection config) {
        serialize(config, this);
    }

    static void serialize(ConfigurationSection config, ISerializable serializable) {
        pushSerializable(config, serializable);
    }

    String TYPE_KEY = "__class__";

    // push primitive objects then dispatch complex objects into corresponding methods
    static void pushMap(ConfigurationSection config, Map<?, ?> map) {
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!(e.getKey() instanceof String)) {
                throw new IllegalArgumentException("Map key is not string: " + e.getKey().toString());
            }
            String key = (String) e.getKey();
            Object obj = e.getValue();
            if (obj == null) continue;
            if (obj instanceof ISerializable) {
                ((ISerializable) obj).serialize(config.createSection(key));
            } else if (obj instanceof Map) {
                pushMap(config.createSection(key), (Map<?, ?>) obj);
            } else if (obj instanceof List) {
                config.set(key, obj);
                //throw new IllegalArgumentException("List serialization not implemented");
            } else if (obj instanceof Enum) {
                config.set(key, ((Enum) obj).name());
            } else {
                config.set(key, obj);
            }
        }
    }

    // Construct shallow map of the object, then call push map.
    // Also deals with StandaloneConfig
    static void pushSerializable(ConfigurationSection config, ISerializable serializable) {
        Class<?> clz = serializable.getClass();
        Map<String, Object> shallowMap = new HashMap<>();
        shallowMap.put(TYPE_KEY, clz.getName());
        for (Field f : clz.getDeclaredFields()) {
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(serializable);
                    } catch (ReflectiveOperationException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize object", ex);
                        standaloneCfg = null;
                    }
                    if (standaloneCfg != null) {
                        standaloneCfg.save();
                        continue;
                    }
                }
            }

            // Normal fields
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null || anno.manualSerialization()) continue;
            f.setAccessible(true);
            String cfgName;
            if (anno.name().equals("")) {
                cfgName = f.getName();
            } else {
                cfgName = anno.name();
                config.set(f.getName(), null);
            }
            for (String key : anno.alias()) {
                config.set(key, null);
            }
            try {
                shallowMap.put(cfgName, f.get(serializable));
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize object", ex);
            }
            pushMap(config, shallowMap);
        }
    }

    // dump all items in the config section into the map, recursively
    // all returned objects are primitive
    static Map<String, ?> popPrimitiveMap(ConfigurationSection config) {
        Map<String, Object> map = new HashMap<>();
        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                map.put(key, popPrimitiveMap(config.getConfigurationSection(key)));
            } else if (config.isList(key)) {
                map.put(key, config.get(key));
                //throw new IllegalArgumentException("List deserialization not supported");
            } else {
                map.put(key, config.get(key));
            }
        }
        return map;
    }

    // deserialize ISerializable from primitive map
    // Also deals with StandaloneConfig
    static void popSerializable(Map<String, ?> primitiveMap, ISerializable serializable) {
        Class<?> clz = serializable.getClass();
        for (Field f : clz.getDeclaredFields()) {
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(serializable);
                    } catch (ReflectiveOperationException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize object", ex);
                        standaloneCfg = null;
                    }
                    if (standaloneCfg != null) {
                        standaloneCfg.load();
                        continue;
                    }
                }
            }

            // Normal fields
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null || anno.manualSerialization()) continue;
            f.setAccessible(true);
            String cfgName = anno.name().equals("") ? f.getName() : anno.name();
            try {
                Object origValue = f.get(serializable);
                Object newPrimitiveValue = null;
                boolean hasValue = false;
                for (String key : anno.alias()) {
                    if (primitiveMap.containsKey(key)) {
                        newPrimitiveValue = primitiveMap.get(key);
                        hasValue = true;
                        break;
                    }
                }
                if (!hasValue && primitiveMap.containsKey(f.getName())) {
                    newPrimitiveValue = primitiveMap.get(f.getName());
                    hasValue = true;
                }
                if (!hasValue && anno.name().length() > 0 && primitiveMap.containsKey(anno.name())) {
                    newPrimitiveValue = primitiveMap.get(anno.name());
                    hasValue = true;
                }
                if (!hasValue) {
                    continue;
                }

                Object newValue = null;
                if (Map.class.isAssignableFrom(f.getType())) {
                    if (!(newPrimitiveValue instanceof Map))
                        throw new IllegalArgumentException("Config field require a map object: " + f.toString());
                    newValue = constructNonPrimitiveMap((Map) newPrimitiveValue);
                } else if (ISerializable.class.isAssignableFrom(f.getType())) {
                    if (!(newPrimitiveValue instanceof Map))
                        throw new IllegalArgumentException("Config field require a map object: " + f.toString());
                    ISerializable obj;
                    try {
                        obj = (ISerializable) f.getType().newInstance();
                    } catch (ReflectiveOperationException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                    popSerializable((Map) newPrimitiveValue, obj);
                    newValue = obj;
                } else if (f.getType().isEnum()) {
                    if (!(newPrimitiveValue instanceof String))
                        throw new IllegalArgumentException("Config field require a string object: " + f.toString());
                    newValue = Enum.valueOf((Class<? extends Enum>) f.getType(), (String) newPrimitiveValue);
                } else {
                    newValue = newPrimitiveValue;
                }

                f.set(serializable, newValue);
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize object", ex);
            }
        }
    }

    // convert primitive objects returned by popPrimitiveMap() into actual objects
    // also recursively reconstruct all the elements in list or map
    // e.g. Map -> ISerializable
    static Object constructNonPrimitiveMap(Map primitiveMap) {
        if (primitiveMap.containsKey(TYPE_KEY)) {
            Class<?> type;
            try {
                type = Class.forName((String) primitiveMap.get(TYPE_KEY));
                if (!ISerializable.class.isAssignableFrom(type)) {
                    throw new IllegalArgumentException("Bad class name: " + primitiveMap.get(TYPE_KEY));
                }
                ISerializable obj = (ISerializable) type.newInstance();
                ISerializable.popSerializable(primitiveMap, obj);
                return obj;
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        Map<Object, Object> nonPrimitiveMap = new HashMap<>();
        for (Object key : primitiveMap.keySet()) {
            Object obj = primitiveMap.get(key);
            if (obj instanceof List) {
                nonPrimitiveMap.put(key, obj);
                //throw new IllegalArgumentException("List deserialization not supported");
            } else if (obj instanceof Map) {
                nonPrimitiveMap.put(key, constructNonPrimitiveMap((Map) obj));
            } else {
                nonPrimitiveMap.put(key, obj);
            }
        }
        return nonPrimitiveMap;
    }
}
