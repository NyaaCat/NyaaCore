package cat.nyaa.nyaacore.utils;

import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.PacketPlayOutSetSlot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtils {
    public static void openWrittenBook(Player player, ItemStack book) {
        if (book != null && book.getType() == Material.WRITTEN_BOOK) {
            sendFakeItemStack(player, player.getInventory().getHeldItemSlot(), book);
            ((CraftPlayer) player).getHandle().a(CraftItemStack.asNMSCopy(book), EnumHand.MAIN_HAND);
            player.updateInventory();
        }
    }

    public static void sendFakeItemStack(Player p, int index, ItemStack itemStack) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        if (player.playerConnection == null) {
            return;
        }
        if (index < net.minecraft.server.v1_14_R1.PlayerInventory.getHotbarSize()) {
            index += 36;
        } else if (index > 39) {
            index += 5;
        } else if (index > 35) {
            index = 8 - (index - 36);
        }
        player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.defaultContainer.windowId, index, CraftItemStack.asNMSCopy(itemStack)));
    }

    public static int getPing(Player p) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        return player.ping;
    }
}
