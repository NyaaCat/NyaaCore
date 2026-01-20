package cat.nyaa.nyaacore.configuration;

import cat.nyaa.nyaacore.configuration.annotation.Deserializer;
import cat.nyaa.nyaacore.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Serialize or Deserialize objects into ConfigurationSection
 */
public interface ISerializable {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void deserialize(ConfigurationSection config, Object obj) {
        Class<?> clz = obj.getClass();
        List<Field> fields = ReflectionUtils.getAllFields(clz);
        for (Field f : fields) {
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(obj);
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
            Deserializer deserializer = f.getAnnotation(Deserializer.class);


            f.setAccessible(true);
            String cfgName = anno.name().equals("") ? f.getName() : anno.name();
            try {
                //Object origValue = f.get(obj);
                Object newValue = null;
                boolean hasValue = false;
                for (String key : anno.alias()) {
                    if (config.contains(key)) {
                        newValue = config.get(key);
                        hasValue = true;
                        break;
                    }
                }
                if (!hasValue && config.contains(f.getName())) {
                    newValue = config.get(f.getName());
                    hasValue = true;
                }
                if (!hasValue && anno.name().length() > 0 && config.contains(anno.name())) {
                    newValue = config.get(anno.name());
                    hasValue = true;
                }
                if (!hasValue) {
                    continue;
                }

                if (deserializer != null && obj instanceof ISerializable) {
                    Setter.from((ISerializable) obj, deserializer.value()).set(newValue);
                }

                if (f.getType().isEnum()) {
                    try {
                        newValue = Enum.valueOf((Class<? extends Enum>) f.getType(), (String) newValue);
                    } catch (Exception ex) {
                        Bukkit.getLogger().warning("Failed to deserialize enum value '" + newValue + "' for field " + f.getName() + " of type " + f.getType().getSimpleName() + ": " + ex.getMessage());
                        continue; // Skip setting this field, keep the default value
                    }
                } else if (Keyed.class.isAssignableFrom(f.getType())) {
                    // Handle registry-based types (Sound, Biome, etc.) that are no longer enums in 1.21+
                    try {
                        // Convert from stored format (BLOCK_ANVIL_USE) to registry key format (block.anvil.use)
                        String keyName = ((String) newValue).toLowerCase().replace('_', '.');
                        // Try minecraft namespace first
                        NamespacedKey key = NamespacedKey.minecraft(keyName);
                        Registry<?> registry = findRegistry(f.getType());
                        if (registry != null) {
                            newValue = registry.get(key);
                            if (newValue == null) {
                                Bukkit.getLogger().warning("Failed to find registry value '" + keyName + "' for field " + f.getName() + " of type " + f.getType().getSimpleName());
                                continue;
                            }
                        } else {
                            Bukkit.getLogger().warning("No registry found for type " + f.getType().getSimpleName() + ", skipping field " + f.getName());
                            continue;
                        }
                    } catch (Exception ex) {
                        Bukkit.getLogger().warning("Failed to deserialize registry value '" + newValue + "' for field " + f.getName() + " of type " + f.getType().getSimpleName() + ": " + ex.getMessage());
                        continue;
                    }
                } else if (ISerializable.class.isAssignableFrom(f.getType())) {
                    if (!(newValue instanceof ConfigurationSection sec))
                        throw new RuntimeException("Map object require ConfigSection: " + f);
                    if (!sec.isString("__class__"))
                        throw new RuntimeException("Missing __class__ key: " + f);
                    String clsName = sec.getString("__class__");
                    Class cls = Class.forName(clsName);
                    ISerializable o = (ISerializable) cls.getDeclaredConstructor().newInstance();
                    o.deserialize(sec);
                    newValue = o;
                    //} else if (List.class.isAssignableFrom(f.getType())) {
                    //    throw new RuntimeException("List serialization is not supported: " + f.toString());
                } else if (Map.class.isAssignableFrom(f.getType())) {
                    if (!(newValue instanceof ConfigurationSection sec))
                        throw new RuntimeException("Map object require ConfigSection: " + f);
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (String key : sec.getKeys(false)) {
                        if (sec.isConfigurationSection(key)) {
                            ConfigurationSection newSec = sec.getConfigurationSection(key);
                            if (!newSec.isString("__class__"))
                                throw new RuntimeException("Missing __class__ key: " + f);
                            String clsName = newSec.getString("__class__");
                            Class cls = Class.forName(clsName);
                            ISerializable o = (ISerializable) cls.getDeclaredConstructor().newInstance();
                            o.deserialize(newSec);
                            map.put(key, o);
                        } else {
                            map.put(key, sec.get(key));
                        }
                    }
                    newValue = map;
                } else if (UUID.class.isAssignableFrom(f.getType())) {
                    if (newValue != null) {
                        newValue = UUID.fromString((String) newValue);
                    }
                }

                f.set(obj, newValue);
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize object", ex);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    static void serialize(ConfigurationSection config, Object obj) {
        Class<?> clz = obj.getClass();
        List<Field> fields = ReflectionUtils.getAllFields(clz);
        for (Field f : fields) {
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(obj);
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
                if (f.getType().isEnum()) {
                    Enum e = (Enum) f.get(obj);
                    if (e == null) continue;
                    config.set(cfgName, e.name());
                } else if (Keyed.class.isAssignableFrom(f.getType())) {
                    // Handle registry-based types (Sound, Biome, etc.) that are no longer enums in 1.21+
                    Keyed k = (Keyed) f.get(obj);
                    if (k == null) continue;
                    config.set(cfgName, k.getKey().getKey().toUpperCase());
                } else if (ISerializable.class.isAssignableFrom(f.getType())) {
                    ISerializable o = (ISerializable) f.get(obj);
                    if (o == null) continue;
                    ConfigurationSection section = config.createSection(cfgName);
                    section.set("__class__", o.getClass().getName());
                    o.serialize(section);
                } else if (Map.class.isAssignableFrom(f.getType())) {
                    Map map = (Map) f.get(obj);
                    if (map == null) continue;
                    ConfigurationSection section = config.createSection(cfgName);
                    for (Object key : map.keySet()) {
                        if (!(key instanceof String k))
                            throw new RuntimeException("Map key is not string: " + f);
                        Object o = map.get(k);
                        if (o instanceof Map || o instanceof List)
                            throw new RuntimeException("Nested Map/List is not allowed: " + f);
                        if (o instanceof ISerializable) {
                            ConfigurationSection newSec = section.createSection(k);
                            newSec.set("__class__", o.getClass().getName());
                            ((ISerializable) o).serialize(newSec);
                        } else {
                            section.set(k, o);
                        }
                    }
                    //} else if (List.class.isAssignableFrom(f.getType())) {
                    //    throw new RuntimeException("List serialization is not supported: " + f.toString());
                } else if (UUID.class.isAssignableFrom(f.getType())) {
                    Object uuidString = f.get(obj);
                    if (uuidString == null) continue;
                    config.set(cfgName, uuidString.toString());
                } else {
                    Object origValue = f.get(obj);
                    if (origValue == null) continue;
                    config.set(cfgName, origValue);
                }
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize object", ex);
            }
        }
    }

    default void deserialize(ConfigurationSection config) {
        deserialize(config, this);
    }

    default void serialize(ConfigurationSection config) {
        serialize(config, this);
    }

    /**
     * Find the appropriate Registry for a given Keyed type.
     * Used for deserializing registry-based types like Sound, Biome, etc.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Keyed> Registry<T> findRegistry(Class<?> type) {
        // Check common registry types
        if (org.bukkit.Sound.class.isAssignableFrom(type)) {
            return (Registry<T>) Registry.SOUNDS;
        } else if (org.bukkit.block.Biome.class.isAssignableFrom(type)) {
            return (Registry<T>) Registry.BIOME;
        } else if (org.bukkit.entity.EntityType.class.isAssignableFrom(type)) {
            return (Registry<T>) Registry.ENTITY_TYPE;
        } else if (org.bukkit.Material.class.isAssignableFrom(type)) {
            return (Registry<T>) Registry.MATERIAL;
        } else if (org.bukkit.enchantments.Enchantment.class.isAssignableFrom(type)) {
            return (Registry<T>) Registry.ENCHANTMENT;
        } else if (org.bukkit.potion.PotionEffectType.class.isAssignableFrom(type)) {
            return (Registry<T>) Registry.POTION_EFFECT_TYPE;
        } else if (org.bukkit.attribute.Attribute.class.isAssignableFrom(type)) {
            return (Registry<T>) Registry.ATTRIBUTE;
        }
        return null;
    }

    /**
     * For informative only
     */
    @Target(ElementType.FIELD)
    @interface Ephemeral {
    }

    /**
     * Indicates a field can be serialized.
     * These four types are supported
     * 1. Primitive types supported by ConfigurationSection (should put() &amp; get() directly)
     * 2. ISerializable
     * //3. List&lt;Type1/2/3/4&gt; (not implemented)
     * 4. Map&lt;String, Type1/2/3/4&gt;
     * NOTE:
     * ENUM without typehint is not supported (ENUM in Map or List) They will be deserialized as string
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Serializable {
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
    @interface StandaloneConfig {
        /**
         * When set to true, this field is ignored
         */
        boolean manualSerialization() default false;
    }

}
