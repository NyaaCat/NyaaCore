package cat.nyaa.nyaacore.timer;

import org.bukkit.plugin.java.JavaPlugin;

public class TimerHandler {
    public final JavaPlugin plugin;
    public final String name;
    private final TimerPersistData data;

    public TimerHandler(JavaPlugin plugin, String name, TimerPersistData data) {
        this.plugin = plugin;
        this.name = name;
        this.data = data;
    }

    public boolean isPaused() {
        return data.isPaused;
    }

    public boolean isDetached() {
        return !TimerManager.instance().timerCallback.containsKey(TimerManager.toInternalName(plugin, name));
    }

    public void pause() {
        //TODO
    }

    public void resume() {
        //TODO
    }
}
