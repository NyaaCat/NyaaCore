package cat.nyaa.nyaacore.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OfflinePlayerUtils {
    private static Map<String, OfflinePlayer> playerCache;

    private OfflinePlayerUtils() {
    }

    public static void init() {
        playerCache = Arrays.stream(Bukkit.getOfflinePlayers())
                            .filter(p -> p.getName() != null)
                            .collect(Collectors.toMap(p -> p.getName().toLowerCase(Locale.ENGLISH), Function.identity(), BinaryOperator.maxBy(Comparator.comparing(OfflinePlayer::getLastPlayed))));
    }

    public static OfflinePlayer lookupPlayer(String name) {
        OfflinePlayer player = Bukkit.getPlayerExact(name);
        if (player != null) return player;
        return playerCache.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static class _Listener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            playerCache.put(event.getPlayer().getName().toLowerCase(Locale.ENGLISH), event.getPlayer());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerQuitEvent event) {
            playerCache.put(event.getPlayer().getName().toLowerCase(Locale.ENGLISH), event.getPlayer());
        }
    }
}
