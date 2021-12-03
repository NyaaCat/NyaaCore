package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.component.IMessageQueue;
import cat.nyaa.nyaacore.component.NyaaComponent;
import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

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
        IMessageQueue.DefaultMessageQueue defaultMessageQueue = new IMessageQueue.DefaultMessageQueue();
        Bukkit.getPluginManager().registerEvents(defaultMessageQueue, this);
        Bukkit.getPluginManager().registerEvents(new ClickSelectionUtils._Listener(), this);
        Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), this);
        NyaaComponent.register(IMessageQueue.class, defaultMessageQueue);
        OfflinePlayerUtils.init();
    }
}
