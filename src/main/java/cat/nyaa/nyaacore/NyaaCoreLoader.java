package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import net.minecraft.SharedConstants;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

public class NyaaCoreLoader extends JavaPlugin {
    private static NyaaCoreLoader instance;

    static {
        ConfigurationSerialization.registerClass(NbtItemStack.class);
    }

    private boolean isTest = false;

//    public NyaaCoreLoader() {
//        super();
//    }
//
//    protected NyaaCoreLoader(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
//        super(loader, description, dataFolder, file);
//    }
//
//    protected NyaaCoreLoader(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file, Boolean isTest) {
//        super(loader, description, dataFolder, file);
//        this.isTest = isTest;
//    }

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
                serverVersion = SharedConstants.getCurrentVersion().name();
            } catch (Exception e) {
                getLogger().severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
            }
            var pluginDescription = getDescription();
            targetVersion = pluginDescription.getAPIVersion();
            getLogger().info("target minecraft version:" + targetVersion + " ,server version:" + serverVersion);
            Bukkit.getPluginManager().registerEvents(new ClickSelectionUtils._Listener(), this);
            Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), this);
            OfflinePlayerUtils.init();
        }
    }

    public static class checkVersion {
        private static checkVersion instance;
        private static boolean bypass = false;

        public static checkVersion getInstance() {
            if (checkVersion.instance == null) {
                checkVersion.instance = new checkVersion();
            }
            return instance;
        }

        public void enable(@Nullable InputStream VersionResource, Logger logger) {
            if (bypass) {
            }

        }

        public void setOff() { // test only
            bypass = true;
        }
    }
}
