package cat.nyaa.nyaacore.utils;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * The idea here is similar to Semantic Versioning (http://semver.org/).
 * But only major & minor version is used.
 *
 * Every time the API is broken, the major version advances by 1 and minor version is reset to 1.
 * Every time a new feature is added, the major version remains unchanged and minor version advances by 1.
 *
 * Since Java Plugins can be compiled against one version of NyaaCore
 * but executed with another version, the desired restrictions are:
 * 1. plugins should not run if compile-time-major-version is different from runtime-major-version
 * 2. plugins should not run if compile-time-minor-version is higher than runtime-minor-version
 *
 * To verify the version requirement, you should copy following code EXACTLY
 * {@code VersionUtils.checkVersion(VersionUtils.API_MAJOR_VERSION, VersionUtils.API_MAJOR_VERSION);}
 *
 * Though I cannot force you to do this.
 * I strongly recommend you put the code in your {@link JavaPlugin#onEnable()} method.
 *
 * NOTE: the API version is not related to the plugin version (at least for now)
 */
public final class VersionUtils {
    public static final int API_MAJOR_VERSION = 2;
    public static final int API_MINOR_VERSION = 1;

    public static void checkVersion(int majorIn, int minorIn) {
        if (majorIn != VersionUtils.API_MAJOR_VERSION) throw new RuntimeException(
                String.format("NyaaCore major version mismatch: compile-time %d, runtime %d", majorIn, VersionUtils.API_MAJOR_VERSION));
        if (minorIn > VersionUtils.API_MINOR_VERSION) throw new RuntimeException(
                String.format("NyaaCore minor version mismatch: compile-time %d, runtime %d", minorIn, VersionUtils.API_MINOR_VERSION));
    }
}
