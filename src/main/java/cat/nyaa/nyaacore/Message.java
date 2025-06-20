package cat.nyaa.nyaacore;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.nyaacore.utils.LocaleUtils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Message {
    public Component inner;

    public Message(String text) {
        inner = Component.text(text);
    }

    public static String getPlayerJson(OfflinePlayer player) {
        return "{name:\"{\\\"text\\\":\\\"" + player.getName() + "\\\"}\",id:\"" + player.getUniqueId() + "\",type:\"minecraft:player\"}";
    }

    public static String getItemJsonStripped(ItemStack item) {
        ItemStack cloned = item.clone();
        if (cloned.hasItemMeta()) {
            var meta = cloned.getItemMeta();
            if(meta instanceof BookMeta) {
                return ItemStackUtils.itemToJson(removeBookContent(cloned));
            }
            if (meta != null) {
                cloned.setItemMeta(filterItemMeta(meta));
            }
        }
        return ItemStackUtils.itemToJson(cloned);
    }


    public static ItemMeta filterItemMeta(ItemMeta itemMeta) {
        var cloned = itemMeta.clone();
        if(!(cloned instanceof BlockStateMeta blockStateMeta))return cloned;
        if(!(blockStateMeta.getBlockState() instanceof InventoryHolder inventoryHolder))return cloned;
        ArrayList<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < inventoryHolder.getInventory().getSize(); i++) {
            ItemStack itemStack = inventoryHolder.getInventory().getItem(i);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                if (items.size() < 5) {
                    if (itemStack.hasItemMeta()) {
                        if (itemStack.getItemMeta().hasLore()) {
                            ItemMeta meta = itemStack.getItemMeta();
                            meta.setLore(new ArrayList<>());
                            itemStack.setItemMeta(meta);
                        }
                        if (itemStack.getItemMeta() instanceof BookMeta) {
                            itemStack = removeBookContent(itemStack);
                        }
                    }
                    items.add(itemStack);
                } else {
                    items.add(new ItemStack(Material.STONE));
                }
            }
        }

        inventoryHolder.getInventory().clear();
        for (int i = 0; i < items.size(); i++) {
            inventoryHolder.getInventory().setItem(i, items.get(i));
        }
        blockStateMeta.setBlockState((BlockState) inventoryHolder);
        return blockStateMeta;

    }

    /**
     * Get a clone of the item where all book pages are removed
     * if not a book, then the same item is returned
     *
     * @param item the book
     * @return book without contents.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static ItemStack removeBookContent(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() instanceof BookMeta) {
            ItemStack itemStack = item.clone();
            BookMeta meta = (BookMeta) itemStack.getItemMeta();
            meta.pages(Component.empty());
            itemStack.setItemMeta(meta);
            return itemStack;
        }
        return item;
    }

    public static void sendActionBarMessage(Player player, Component msg) {
        player.sendActionBar(msg);
    }

    public static void sendTitle(Player player, Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Title title1 = Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(fadeInTicks * 50L), Duration.ofMillis(stayTicks * 50L), Duration.ofMillis(fadeOutTicks * 50L)));
        player.showTitle(title1);
    }

    public Message append(String text) {
        inner = inner.append(Component.text(text));
        return this;
    }

    public Message appendFormat(LanguageRepository i18n, String template, Object... obj) {
        return append(i18n.getFormatted(template, obj));
    }

    public Message append(ItemStack item) {
        return append("{itemName} *{amount}", item);
    }

    /**
     * supported syntax
     * {itemName}: when cursor hovered on, item will be displayed, item at index=0
     * {itemName:idx}: the number indicates the index of the item in items list
     * {amount}: a number, item at index=0
     * {amount:idx}: a number, item at index=idx (e.g {amount:0})
     *
     * @param template the template string
     * @param items    item list
     * @return the Message
     */
    public Message append(String template, ItemStack... items) {
        if (items == null || items.length == 0) return this;
        Map<String, Component> varMap = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack clone = items[i].clone();
            boolean hasCustomName = clone.hasItemMeta() && clone.getItemMeta().hasCustomName();
            Component cmp = hasCustomName ? clone.displayName() : Component.translatable(clone);
            varMap.put(String.format("{amount:%d}", i), Component.text(clone.getAmount()));
            varMap.put(String.format("{itemName:%d}", i), cmp);
            if (i == 0) {
                varMap.put("{amount}", Component.text(clone.getAmount()));
                varMap.put("{itemName}", cmp);
            }
        }

        return append(template, varMap);
    }

    public Message append(String template, Map<String, Component> varMap) {
        String remTemplate = template;
        while (!remTemplate.isEmpty()) {
            int idx = remTemplate.length();
            String var = null;
            for (String v : varMap.keySet()) {
                int t = remTemplate.indexOf(v);
                if (t >= 0 && t < idx) {
                    idx = t;
                    var = v;
                }
            }

            if (idx == 0) {
                remTemplate = remTemplate.substring(var.length());
                append(varMap.get(var));
            }
            if (idx > 0) {
                append(remTemplate.substring(0, idx));
                remTemplate = remTemplate.substring(idx);
            }
        }
        return this;
    }

    public Message append(Component component) {
        inner = inner.append(component);
        return this;
    }

    public Message send(CommandSender p) {
        if (p instanceof Player) {
            return send((Player) p);
        } else {
            p.sendMessage(this.inner);
            return this;
        }
    }

    public Message send(Player p) {
        return send(p.getPlayer(), MessageType.CHAT);
    }

    public Message send(Player p, MessageType type) {
        if (type == MessageType.CHAT) {
            p.sendMessage(inner);
        } else if (type == MessageType.ACTION_BAR) {
            sendActionBarMessage(p, inner);
        } else if (type == MessageType.TITLE) {
            sendTitle(p, inner, Component.empty(), 10, 40, 10);
        } else if (type == MessageType.SUBTITLE) {
            sendTitle(p, Component.empty(), inner, 10, 40, 10);
        }
        return this;
    }

    public Message broadcast() {
        return broadcast(MessageType.CHAT);
    }

    public Message broadcast(MessageType type) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            send(p, type);
        }
        Bukkit.getConsoleSender().sendMessage(inner);
        return this;
    }

    public Message broadcast(Permission permission) {
        Server server = Bukkit.getServer();
        server.broadcast(inner, permission.getName());
        return this;
    }

    public Message broadcast(World world) {
        world.sendMessage(inner);
        Bukkit.getConsoleSender().sendMessage(inner);
        return this;
    }

    public Message broadcast(MessageType type, Predicate<Player> playerFilter) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerFilter.test(player)) {
                this.send(player, type);
            }
        }
        Bukkit.getConsoleSender().sendMessage(inner);
        return this;
    }

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(inner);
    }

    public enum MessageType {CHAT, ACTION_BAR, TITLE, SUBTITLE}
}