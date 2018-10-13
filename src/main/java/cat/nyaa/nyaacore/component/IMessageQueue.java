package cat.nyaa.nyaacore.component;

import cat.nyaa.nyaacore.Message;
import org.bukkit.OfflinePlayer;

public interface IMessageQueue extends IComponent {
    void send(OfflinePlayer player, Message message);
}
