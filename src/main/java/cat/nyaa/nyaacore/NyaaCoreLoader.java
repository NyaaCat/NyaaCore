package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.component.IMessageQueue;
import cat.nyaa.nyaacore.component.NyaaComponent;
import cat.nyaa.nyaacore.configuration.NbtItemStack;
import cat.nyaa.nyaacore.http.client.HttpClient;
import cat.nyaa.nyaacore.timer.TimerManager;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.plugin.java.JavaPlugin;

public class NyaaCoreLoader extends JavaPlugin {
    public static final String TARGET_MAPPING = "c2d5d7871edcc4fb0f81d18959c647af";
    private static NyaaCoreLoader instance;

    static {
        ConfigurationSerialization.registerClass(NbtItemStack.class);
    }

    public TimerManager timerManager;

    public static NyaaCoreLoader getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        LanguageRepository.initInternalMap(this);
        //timerManager = new TimerManager(this);
    }

    @Override
    public void onEnable() {
        try {
            boolean check = MappingChecker.check();
            if (!check) {
                getLogger().severe("Unsupported NMS Mapping version detected. Unexpected error may occurred.");
            }
        } catch (NoSuchMethodError e) {
            getLogger().info("Can not detect CraftBukkit NMS Mapping version. Unexpected error may occurred.");
        }
        HttpClient.init(0);
        IMessageQueue.DefaultMessageQueue defaultMessageQueue = new IMessageQueue.DefaultMessageQueue();
        Bukkit.getPluginManager().registerEvents(defaultMessageQueue, this);
        Bukkit.getPluginManager().registerEvents(new ClickSelectionUtils._Listener(), this);
        Bukkit.getPluginManager().registerEvents(new OfflinePlayerUtils._Listener(), this);
        NyaaComponent.register(IMessageQueue.class, defaultMessageQueue);
        OfflinePlayerUtils.init();
        //timerManager.load();
    }

    @Override
    public void onDisable() {
        HttpClient.shutdown();
        //timerManager.save();
    }

    private static class MappingChecker {
        static boolean check() {
            String mappingsVersion = ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion();
            return TARGET_MAPPING.equals(mappingsVersion);
        }
    }
}
