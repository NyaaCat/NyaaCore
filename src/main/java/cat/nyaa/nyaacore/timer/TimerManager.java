package cat.nyaa.nyaacore.timer;

import cat.nyaa.nyaacore.NyaaCoreLoader;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * The timers works similar to runTaskLater or runTaskTimer.
 * Except it's based on real time rather than ticks and
 * is more flexible about server downtime.
 * <p>
 * A timer can be DETACHED and/or PAUSED
 * DETACHED: A timer is DETACHED when TimerManager doesn't know what's the timer's callback function.
 * This may because the server has just started and plugin hasn't register the callback yet.
 * When the server is down, all timers are DETACHED
 * PAUSED:   A timer is PAUSED when it's not counting time.
 * A timer can be NOT PAUSED even when the server is down.
 * <p>
 * //TODO: use bukkit events instead of callbacks
 */
public class TimerManager extends FileConfigure {
    private static final ITimerCallback dummyTimerCallback = (a, b, c, d) -> {
    };
    private final NyaaCoreLoader plugin;
    @Serializable
    Map<String, TimerPersistData> timerData;
    Map<String, ITimerCallback> timerCallback;
    Map<String, ITimerCallback> timerResetCallback;

    public TimerManager(NyaaCoreLoader plugin) {
        this.plugin = plugin;
    }

    public static TimerManager instance() {
        throw new RuntimeException("Timer subsystem is not implemented");
        //return NyaaCoreLoader.getInstance().timerManager;
    }

    /**
     * Convert (plugin, timerName) pair to internal timer name string
     */
    static String toInternalName(JavaPlugin plugin, String timerName) {
        return plugin.getName() + "!" + timerName;
    }

    @Override
    protected String getFileName() {
        return "timers.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Timers are uniquely distinguished by plugin &amp; timerName.
     * This method link the existing or newly created timer to the callback function.
     *
     * @param plugin    the plugin the timer belongs to
     * @param timerName the timer name, usually plugin specific
     * @param timerData the timer data &amp; parameters, only for newly created timers.
     *                  for existing timers, you should leave it to null otherwise exception will be thrown
     * @param callback  the callback object for the timer.
     */
    public void registerTimer(JavaPlugin plugin, String timerName, TimerData timerData, ITimerCallback callback, ITimerCallback timerResetCallback) {
        String internalName = toInternalName(plugin, timerName);
        if (this.timerData.containsKey(internalName)) {
            if (timerData != null) throw new IllegalArgumentException("Cannot overwrite timerData of existing timer");
        } else {
            TimerPersistData data = new TimerPersistData(timerData);
            data.creationTime = Instant.now();
            data.lastTimerCallback = Instant.MIN;
            data.lastResetCallback = Instant.MIN;
            data.lastCheckpoint = Instant.now();
            data.timeElapsed = Duration.ZERO;
            this.timerData.put(internalName, data);
            save();
        }
        timerCallback.put(internalName, callback == null ? dummyTimerCallback : callback);
        this.timerResetCallback.put(internalName, timerResetCallback == null ? dummyTimerCallback : timerResetCallback);

        //todo check callback status
    }

    /**
     * Check if the timer is already registered.
     *
     * @param plugin
     * @param timerName
     * @return
     */
    public boolean timerExists(JavaPlugin plugin, String timerName) {
        return timerData.containsKey(toInternalName(plugin, timerName));
    }

    /**
     * remove a registered timer
     *
     * @param plugin
     * @param timerName
     */
    public void removeTimer(JavaPlugin plugin, String timerName) {
        // todo check call back
        String internalName = toInternalName(plugin, timerName);
        timerData.remove(internalName);
        timerCallback.remove(internalName);
        timerResetCallback.remove(internalName);
        save();
    }

    public TimerHandler getTimer(JavaPlugin plugin, String timerName) {
        TimerPersistData data = timerData.get(toInternalName(plugin, timerName));
        return data == null ? null : new TimerHandler(plugin, timerName, data);
    }
}
