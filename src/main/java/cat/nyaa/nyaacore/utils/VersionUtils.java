package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;

import java.util.*;

public class VersionUtils {
    protected static int MAX_CACHE_SIZE = 10;
    private static final LinkedHashMap<String, int[]> VersionCache = new LinkedHashMap<>((int) Math.ceil(MAX_CACHE_SIZE / 0.75) + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, int[]> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public static String getCurrentVersion() {
        return Bukkit.getBukkitVersion();
    }

    public static boolean isVersionEqual(String versionStr, String otherStr) {
        return Arrays.equals(splitVersionStringToIntArray(versionStr), splitVersionStringToIntArray(otherStr));
    }

    public static boolean isVersionGreaterOrEq(String versionStr, String otherStr) {
        int[] versionInt = splitVersionStringToIntArray(versionStr);
        int[] otherInt = splitVersionStringToIntArray(otherStr);
        if (Arrays.equals(versionInt, otherInt)) return true; // =
        for (int i = 0; i < otherInt.length; i++) {
            if (versionInt.length <= i) {
                return false;//<
            }
            if (versionInt[i] < otherInt[i]) return false;//>
        }
        return versionInt.length >= otherInt.length;
    }

    public synchronized static int[] splitVersionStringToIntArray(String version) {
        int[] result;
        if ((result = VersionCache.get(version)) != null) return result;
        result = splitVersionStringToIntegerList(version).stream().mapToInt(i -> i).toArray();
        VersionCache.put(version, result);
        return result;
    }

    private static List<Integer> splitVersionStringToIntegerList(String version) {
        String[] splitVersion = splitVersionString(version);
        List<Integer> result = new ArrayList<>();
        for (String s : splitVersion) {
            int versionint;
            try {
                versionint = Integer.parseInt(s.replaceAll("[^\\d]", ""));
            } catch (NumberFormatException ignored) {
                versionint = 0;
            }
            result.add(versionint);
        }
        while (result.lastIndexOf(0) == (result.size() - 1)) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    public static String[] splitVersionString(String version) {
        version = version.replace('-', '.');
        version = version.replace('_', '.');
        version = version.replace('R', '.');
        String[] splitVersion = version.split("\\.");
        List<String> result = new ArrayList<>();
        for (String s : splitVersion) {
            if (!s.equals("")) result.add(s);
        }
        return result.toArray(new String[0]);
    }
}
