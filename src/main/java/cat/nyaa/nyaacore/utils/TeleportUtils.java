package cat.nyaa.nyaacore.utils;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TeleportUtils {
    @Deprecated
    public static boolean Teleport(Player player, Location loc) {
        if (!player.isOnline() || loc == null || loc.getWorld() == null) {
            return false;
        }
        Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (ess != null) {
            try {
                ess.getUser(player).getAsyncTeleport().now(loc, false, PlayerTeleportEvent.TeleportCause.PLUGIN, CompletableFuture.completedFuture(true));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            player.setFallDistance(0);
            player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            return true;
        }
    }

    @Deprecated
    public static void Teleport(List<Player> players, Location loc) {
        for (Player p : players) {
            Teleport(p, loc);
        }
    }
}
