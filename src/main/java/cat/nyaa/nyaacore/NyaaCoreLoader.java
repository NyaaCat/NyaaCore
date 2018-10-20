package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.component.IMessageQueue;
import cat.nyaa.nyaacore.component.NyaaComponent;
import cat.nyaa.nyaacore.http.client.HttpClient;
import cat.nyaa.nyaacore.timer.TimerManager;
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
        NyaaComponent.register(IMessageQueue.class, defaultMessageQueue);
        //timerManager.load();
    }

    @Override
    public void onDisable() {
        HttpClient.shutdown();
        //timerManager.save();
    }
}
