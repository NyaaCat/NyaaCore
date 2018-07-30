/*
 * Copyright (C) SainttX <http://sainttx.com>
 * Copyright (C) contributors
 *
 * This file is part of Auctions.
 *
 * Auctions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Auctions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Auctions.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionUtils {

    /*
     * The server version string to location NMS & OBC classes
     */
    private static String versionString;

    /*
     * Cache of NMS classes that we've searched for
     */
    private static Map<String, Class<?>> loadedNMSClasses = new HashMap<>();

    /*
     * Cache of OBS classes that we've searched for
     */
    private static Map<String, Class<?>> loadedOBCClasses = new HashMap<>();

    /*
     * Cache of methods that we've found in particular classes
     */
    private static Map<Class<?>, Map<String, Method>> loadedMethods = new HashMap<>();

    /**
     * Gets the version string for NMS & OBC class paths
     *
     * @return The version string of OBC and NMS packages
     */
    public static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }

    /**
     * Get an NMS Class
     *
     * @param nmsClassName The name of the class
     * @return The class
     */
    public static Class<?> getNMSClass(String nmsClassName) {
        if (loadedNMSClasses.containsKey(nmsClassName)) {
            return loadedNMSClasses.get(nmsClassName);
        }

        String clazzName = "net.minecraft.server." + getVersion() + nmsClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return loadedNMSClasses.put(nmsClassName, null);
        }

        loadedNMSClasses.put(nmsClassName, clazz);
        return clazz;
    }

    /**
     * Get a class from the org.bukkit.craftbukkit package
     *
     * @param obcClassName the path to the class
     * @return the found class at the specified path
     */
    public static Class<?> getOBCClass(String obcClassName) {
        if (loadedOBCClasses.containsKey(obcClassName)) {
            return loadedOBCClasses.get(obcClassName);
        }

        String clazzName = "org.bukkit.craftbukkit." + getVersion() + obcClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            loadedOBCClasses.put(obcClassName, null);
            return null;
        }

        loadedOBCClasses.put(obcClassName, clazz);
        return clazz;
    }

    /**
     * Get a method from a class that has the specific parameters
     *
     * @param clazz      The class we are searching
     * @param methodName The name of the method
     * @param params     Any parameters that the method has
     * @return The method with appropriate parameters
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        if (!loadedMethods.containsKey(clazz)) {
            loadedMethods.put(clazz, new HashMap<>());
        }

        Map<String, Method> methods = loadedMethods.get(clazz);

        if (methods.containsKey(methodName)) {
            return methods.get(methodName);
        }

        try {
            Method method = clazz.getDeclaredMethod(methodName, params);
            if (method != null) method.setAccessible(true);
            methods.put(methodName, method);
            loadedMethods.put(clazz, methods);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            methods.put(methodName, null);
            loadedMethods.put(clazz, methods);
            return null;
        }
    }

    /**
     * @deprecated use Material.isItem();
     */
    @Deprecated
    public static boolean isValidItem(ItemStack item) {
        Class<?> craftItemStackClazz = ReflectionUtils.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtils.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
        Object nmsItemStackObj = null;
        try {
            nmsItemStackObj = asNMSCopyMethod.invoke(null, item);
            if (nmsItemStackObj == null) {
                return false;
            } else {
                Method isEmptyMethod = ReflectionUtils.getMethod(nmsItemStackObj.getClass(), "isEmpty");
                return !((boolean) isEmptyMethod.invoke(nmsItemStackObj));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static String convertItemStackToJson(ItemStack itemStack) throws RuntimeException {
        return ItemStackUtils.itemToJson(itemStack);
    }
}
