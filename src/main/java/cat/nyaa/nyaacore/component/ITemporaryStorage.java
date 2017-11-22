package cat.nyaa.nyaacore.component;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Temporary storage is used to deliver items to players reliably.
 * Usually, when giving items to players,
 * they can be drop at the player's location,
 * put into a player's inventory or ender chest.
 * But the player may be in a danger zone (e.g. swimming in lava)
 * or the inventories could be full.
 * <p>
 * Temporary storage is designed to be a safe place with unlimited capacity
 * so players need no more to worry about losing their items.
 * <p>
 * See https://github.com/NyaaCat/HamsterEcoHelper/blob/master/src/main/java/cat/nyaa/HamsterEcoHelper/utils/database/Database.java#L75
 */
public interface ITemporaryStorage extends IComponent {
    /**
     * Put items into a player's storage
     *
     * @param player the player
     * @param items  items to be given to the player
     */
    void add(OfflinePlayer player, Collection<ItemStack> items);

    /**
     * Put a item into a player's storage
     *
     * @param player the player
     * @param item   item to be given to the player
     */
    default void add(OfflinePlayer player, ItemStack item) {
        add(player, Collections.singletonList(item));
    }

    /**
     * Get stored items for a player.
     * Can be an empty list.
     *
     * @param player the player
     * @return a list of deep copies of stored items for that player
     */
    List<ItemStack> get(OfflinePlayer player);

    /**
     * Remove all stored items for a player
     *
     * @param player the player
     */
    void clear(OfflinePlayer player);

    /**
     * Check if the player has stored items
     *
     * @param player the player
     * @return true if there are items stored for that player, false otherwise
     */
    default boolean hasStoredItem(OfflinePlayer player) {
        return get(player).size() > 0;
    }

    /**
     * Reset a player's stored item list
     *
     * @param player the player
     * @param items  player's new item collection
     * @return old stored items, can be a shallow copy
     */
    default List<ItemStack> set(OfflinePlayer player, Collection<ItemStack> items) {
        List<ItemStack> ret = get(player);
        clear(player);
        add(player, items);
        return ret;
    }
}
