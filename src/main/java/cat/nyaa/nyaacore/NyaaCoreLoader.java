package cat.nyaa.nyaacore;

import org.bukkit.plugin.java.JavaPlugin;

public class NyaaCoreLoader extends JavaPlugin {
    @Override
    public void onLoad() {
        LanguageRepository.initInternalMap(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }
}
