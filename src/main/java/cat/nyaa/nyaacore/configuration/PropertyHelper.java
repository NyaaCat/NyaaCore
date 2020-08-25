package cat.nyaa.nyaacore.configuration;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropertyHelper {
    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    public static void setProperty(CommandSender sender, ISerializable power, Field field, String value) {
        try {
            if (value.equals("null")) {
                field.set(power, null);
                return;
            }
            field.setAccessible(true);
            if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                try {
                    field.set(power, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_int", value);
                }
            } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                try {
                    field.set(power, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_int", value);
                }
            } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                try {
                    field.set(power, Float.parseFloat(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_double", value);
                }
            } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                try {
                    field.set(power, Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_double", value);
                }
            } else if (field.getType().equals(String.class)) {
                field.set(power, value);
            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    field.set(power, Boolean.valueOf(value));
                } else {
                    throw new BadCommandException("message.error.invalid_option", value, field.getName(), "true, false");
                }
            } else if (field.getType().isEnum()) {
                try {
                    field.set(power, Enum.valueOf((Class<Enum>) field.getType(), value));
                } catch (IllegalArgumentException e) {
                    throw new BadCommandException("internal.error.bad_enum", field.getName(), Stream.of(field.getType().getEnumConstants()).map(Object::toString).collect(Collectors.joining(", ")));
                }
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> listArg = (Class<?>) listType.getActualTypeArguments()[0];
                String[] valueStrs = value.split(",");
                Stream<String> values = Arrays.stream(valueStrs).filter(s -> !s.isEmpty()).map(String::trim);
                if (field.getType().equals(List.class)) {
                    if (listArg.isEnum()) {
                        Class<? extends Enum> enumClass = (Class<? extends Enum>) listArg;
                        Stream<Enum> enumStream = values.map(v -> Enum.valueOf(enumClass, v));
                        List<Enum> list = enumStream.collect(Collectors.toList());
                        field.set(power, list);
                    } else if (listArg.equals(String.class)) {
                        List<String> list = values.collect(Collectors.toList());
                        field.set(power, list);
                    } else if (listArg.equals(Integer.class)) {
                        List<Integer> list = values.map(Integer::parseInt).collect(Collectors.toList());
                        field.set(power, list);
                    } else if (listArg.equals(Double.class)) {
                        List<Double> list = values.map(Double::parseDouble).collect(Collectors.toList());
                        field.set(power, list);
                    } else {
                        throw new BadCommandException("internal.error.command_exception");
                    }
                } else {
                    if (listArg.isEnum()) {
                        Class<? extends Enum> enumClass = (Class<? extends Enum>) listArg;
                        Stream<Enum> enumStream = values.map(v -> Enum.valueOf(enumClass, v));
                        Set<Enum> set = enumStream.collect(Collectors.toSet());
                        field.set(power, set);
                    } else if (listArg.equals(String.class)) {
                        Set<String> set = values.collect(Collectors.toSet());
                        field.set(power, set);
                    } else if (listArg.equals(Integer.class)) {
                        Set<Integer> list = values.map(Integer::parseInt).collect(Collectors.toSet());
                        field.set(power, list);
                    } else if (listArg.equals(Double.class)) {
                        Set<Double> list = values.map(Double::parseDouble).collect(Collectors.toSet());
                        field.set(power, list);
                    } else {
                        throw new BadCommandException("internal.error.command_exception");
                    }
                }
            } else if (field.getType() == ItemStack.class) {
                Material m = getMaterial(value);
                ItemStack item;
                if (sender instanceof Player && value.equalsIgnoreCase("HAND")) {
                    ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
                    if (hand == null || hand.getType() == Material.AIR) {
                        throw new BadCommandException("message.error.iteminhand");
                    }
                    item = hand.clone();
                } else if (m == null || m == Material.AIR || !m.isItem()) {
                    throw new BadCommandException("message.error.material", value);
                } else {
                    item = new ItemStack(m);
                }
                field.set(power, item.clone());
            } else if (field.getType() == Enchantment.class) {
                Enchantment enchantment;
                if (VALID_KEY.matcher(value).matches()) {
                    enchantment = Enchantment.getByKey(NamespacedKey.minecraft(value));
                } else if (value.contains(":")) {
                    if (value.startsWith("minecraft:")) {
                        enchantment = Enchantment.getByKey(NamespacedKey.minecraft(value.split(":", 2)[1]));
                    } else {
                        enchantment = Enchantment.getByKey(new NamespacedKey(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(value.split(":", 2)[0])), value.split(":", 2)[1]));
                    }
                } else {
                    enchantment = Enchantment.getByName(value);
                }
                if (enchantment == null) {
                    enchantment = Arrays.stream(Enchantment.class.getDeclaredFields()).parallel().filter(f -> Modifier.isStatic(f.getModifiers())).filter(f -> f.getName().equals(value)).findAny().map(f -> {
                        try {
                            return (Enchantment) f.get(null);
                        } catch (IllegalAccessException e) {
                            throw new BadCommandException("message.error.invalid_enchant", e);
                        }
                    }).orElse(null);
                }
                field.set(power, enchantment);
            } else {
                throw new BadCommandException("message.error.invalid_command_arg");
            }
        } catch (IllegalAccessException e) {
            throw new BadCommandException("internal.error.command_exception", e);
        }
    }

    /**
     * return a list of properties in a iSerializable class
     * formatted in {@Code <property_name>:<value>}
     * safe to use by {@Link setUnchecked()}
     *
     * @param iSerializableClass class to resolve
     * @return a List of String that can be used in tab completion.
     */
    public static Collection<? extends String> resolveProperties(Class<? extends ISerializable> iSerializableClass) {
        List<String> list = new ArrayList<>();
        Field[] f = iSerializableClass.getDeclaredFields();
        if (f.length > 0) {
            for (Field field : f) {
                ISerializable.Serializable ann = field.getAnnotation(ISerializable.Serializable.class);
                if (ann != null) {
                    list.add(field.getName() + ":");
                }
            }
        }
        Class<?> superclass = iSerializableClass.getSuperclass();
        if (superclass != null && superclass.isAssignableFrom(ISerializable.class)) {
            list.addAll(resolveProperties((Class<? extends ISerializable>) superclass));
        }
        return list;
    }

    private static Material getMaterial(String name) {
        Material m = Material.matchMaterial(name, false);
        if (m == null) {
            m = Material.matchMaterial(name, true);
            if (m != null) {
                m = Bukkit.getUnsafe().fromLegacy(m);
                throw new BadCommandException("message.error.invalid_command_arg");
            }
        }
        return m;
    }

    public void setProperty(CommandSender sender, Arguments arguments, ISerializable resourceConfig) {
        String inst = arguments.nextString();
        String[] split = inst.split(":", 2);

        Class<? extends ISerializable> aClass = resourceConfig.getClass();
        List<Field> allFields = ReflectionUtils.getAllFields(aClass);
        Field declaredField = allFields.stream()
                .filter(field -> field.getName().equals(split[0]))
                .findFirst().orElseThrow(BadCommandException::new);
        String obj = split[1];
        setProperty(sender, resourceConfig, declaredField, obj);
    }

}
