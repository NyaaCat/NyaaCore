package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.utils.L10nUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class NyaaCoreLoader extends JavaPlugin {
    @Override
    public void onLoad() {
        LanguageRepository.initInternalMap(this);
        L10nUtils.init(this);
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
