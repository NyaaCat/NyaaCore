package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import net.minecraft.SharedConstants;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Logger;

public class NyaaCoreLoader extends JavaPlugin {
    private static NyaaCoreLoader instance;

    static {
        ConfigurationSerialization.registerClass(NbtItemStack.class);
    }

    private boolean isTest = false;

    public NyaaCoreLoader() {
        super();
    }

    protected NyaaCoreLoader(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    protected NyaaCoreLoader(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file, Boolean isTest) {
        super(loader, description, dataFolder, file);
        this.isTest = isTest;
    }

    public static NyaaCoreLoader getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!isTest) {
            String serverVersion = "", targetVersion;
            try {
                serverVersion = SharedConstants.getCurrentVersion().getName();
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin((Plugin) this);
            }
            try {
                var VersionResource = getResource("MCVersion");
                targetVersion = VersionResource == null ? "" : Arrays.toString(VersionResource.readAllBytes());
                getLogger().info("target minecraft version:" + targetVersion + "server version:" + serverVersion);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Bukkit.getPluginManager().registerEvents(new ClickSelectionUtils._Listener(), this);
            Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), this);
            OfflinePlayerUtils.init();
        }
    }

    public static class checkVersion {
        private static checkVersion instance;
        private static boolean bypass = false;

        public void enable(@Nullable InputStream VersionResource, Logger logger) {
            if (bypass) return;

        }

        public void setOff() { // test only
            bypass = true;
        }

        public static checkVersion getInstance() {
            if (checkVersion.instance == null) {
                checkVersion.instance = new checkVersion();
            }
            return instance;
        }
    }
}
