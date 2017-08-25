package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.timer.TimerManager;
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
        //timerManager.load();
    }

    @Override
    public void onDisable() {
        //timerManager.save();
    }
}
