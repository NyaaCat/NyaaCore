package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.utils.VersionUtils;
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

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        VersionUtils.checkVersion(VersionUtils.API_MAJOR_VERSION, VersionUtils.API_MAJOR_VERSION);
    }
}
