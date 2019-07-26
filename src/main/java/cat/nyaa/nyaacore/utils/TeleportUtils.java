package cat.nyaa.nyaacore.utils;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public final class TeleportUtils {

    public static boolean Teleport(Player player, Location loc) {
        if (!player.isOnline() || loc == null || loc.getWorld() == null) {
            return false;
        }
        Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (ess != null) {
            try {
                ess.getUser(player).getTeleport().now(loc, false, PlayerTeleportEvent.TeleportCause.PLUGIN);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            player.setFallDistance(0);
            player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            return true;
        }
    }

    public static void Teleport(List<Player> players, Location loc) {
        for (Player p : players) {
            Teleport(p, loc);
        }
    }
}
