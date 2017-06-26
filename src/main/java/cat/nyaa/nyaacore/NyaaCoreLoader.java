package cat.nyaa.nyaacore;

import org.bukkit.plugin.java.JavaPlugin;

public class NyaaCoreLoader extends JavaPlugin {
    private static NyaaCoreLoader instance;
    public static NyaaCoreLoader getInstance() {
        return instance;
    }
    @Override
    public void onLoad() {
        instance = this;
        LanguageRepository.initInternalMap(this);
    }
}
