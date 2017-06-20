package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.utils.L10nUtils;
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
