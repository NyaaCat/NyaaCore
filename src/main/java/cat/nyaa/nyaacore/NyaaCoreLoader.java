package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Minecart;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

public class NyaaCoreLoader extends JavaPlugin {
    private static NyaaCoreLoader instance;

    static {
        ConfigurationSerialization.registerClass(NbtItemStack.class);
    }

    public NyaaCoreLoader() {
        super();
    }

    protected NyaaCoreLoader(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
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
        String serverVersion="",targetVersion;
        try {
            serverVersion= SharedConstants.getCurrentVersion().getName();
        }catch (Exception e){
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        try {
            InputStream VersionResource = getResource("MCVersion");
            targetVersion =VersionResource == null ? "" : Arrays.toString(VersionResource.readAllBytes());
            getLogger().info("target minecraft version:"+targetVersion+"server version:"+serverVersion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents(new ClickSelectionUtils._Listener(), this);
        Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), this);
        OfflinePlayerUtils.init();
    }
}
