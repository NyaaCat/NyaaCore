package cat.nyaa.nyaacore.utils;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TeleportUtils {

    public static void Teleport(Player player, Location loc) {
        if (!player.isOnline()) {
            return;
        }
        Essentials ess = null;
        if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") != null) {
            ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        }
        if (ess != null) {
            try {
                ess.getUser(player).getTeleport().now(loc, false, PlayerTeleportEvent.TeleportCause.PLUGIN);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.setFallDistance(0);
            player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    public static void Teleport(List<Player> players, Location loc) {
        for (Player p : players) {
            Teleport(p, loc);
        }
    }
}
