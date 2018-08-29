package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.http.client.HttpClient;
import cat.nyaa.nyaacore.timer.TimerManager;
import org.bukkit.plugin.java.JavaPlugin;

import static java.nio.charset.StandardCharsets.UTF_8;

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
        //timerManager.load();
    }

    @Override
    public void onDisable() {
        HttpClient.shutdown();
        //timerManager.save();
    }
}
