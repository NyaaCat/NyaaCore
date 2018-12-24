package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.component.IMessageQueue;
import cat.nyaa.nyaacore.component.NyaaComponent;
import cat.nyaa.nyaacore.http.client.HttpClient;
import cat.nyaa.nyaacore.timer.TimerManager;
import cat.nyaa.nyaacore.utils.ClickSelectionUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NyaaCoreLoader extends JavaPlugin {
    private static NyaaCoreLoader instance;
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
}
