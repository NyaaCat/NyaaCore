package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ConcurrentUtils {
    public static <P,Q> void runAsyncTask(Plugin plugin, P parameter, Function<P,Q> asyncTask, Consumer<Q> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            final Q ret = asyncTask.apply(parameter);
            Bukkit.getScheduler().runTask(plugin, ()->{
                callback.accept(ret);
            });
        });
    }

    public static <P> void runAsyncTask(Plugin plugin, P parameter, Consumer<P> asyncTask) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            asyncTask.accept(parameter);
        });
    }
}
