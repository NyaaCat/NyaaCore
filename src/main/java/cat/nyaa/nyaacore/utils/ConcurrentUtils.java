package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ConcurrentUtils {
    /**
     * Execute a task asynchronously then execute the callback synchronously
     */
    public static <P, Q> void runAsyncTask(Plugin plugin, P parameter, Function<P, Q> asyncTask, Consumer<Q> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final Q ret = asyncTask.apply(parameter);
            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.accept(ret);
            });
        });
    }

    /**
     * @deprecated caller can use {@link org.bukkit.scheduler.BukkitScheduler#runTaskAsynchronously(Plugin, Runnable)} directly
     */
    @Deprecated
    public static <P> void runAsyncTask(Plugin plugin, P parameter, Consumer<P> asyncTask) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            asyncTask.accept(parameter);
        });
    }
}
