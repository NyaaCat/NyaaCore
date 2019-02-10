package cat.nyaa.nyaacore.utils;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class InventoryUtils {

    public static boolean hasItem(Player player, ItemStack item, int amount) {
        return hasItem(player.getInventory(), item, amount);
    }

    public static boolean hasItem(Inventory inv, ItemStack item, int amount) {
        return inv.containsAtLeast(item, amount);
    }

    public static boolean addItem(Player player, ItemStack item) {
        return addItem(player.getInventory(), item.clone(), item.getAmount());
    }

    public static boolean addItem(Inventory inventory, ItemStack item) {
        return addItem(inventory, item.clone(), item.getAmount());
    }

    private static boolean addItem(Inventory inventory, ItemStack item, int amount) {
        ItemStack i = item.clone();
        i.setAmount(amount);
        return addItems(inventory, Collections.singletonList(i));
    }

    public static boolean addItems(Inventory inventory, List<ItemStack> items) {
        return _addItems(inventory, items.stream().filter(i -> i != null && i.getType() != Material.AIR).map(ItemStack::clone).collect(Collectors.toList()));
    }

    private static boolean _addItems(Inventory inventory, List<ItemStack> items) {
        ItemStack[] tmpInv = new ItemStack[inventory.getSize()];
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= 36 && i <= 39 && inventory instanceof PlayerInventory) {
                tmpInv[i] = null;
                continue;
            }
            if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                tmpInv[i] = inventory.getItem(i).clone();
            } else {
                tmpInv[i] = new ItemStack(Material.AIR);
            }
        }
        for (ItemStack item : items) {
            int amount = item.getAmount();
            for (int slot = 0; slot < tmpInv.length; slot++) {
                ItemStack tmp = tmpInv[slot];
                if (tmp == null) {
                    continue;
                }
                if (tmp.getAmount() < item.getMaxStackSize() && item.isSimilar(tmp)) {
                    if ((tmp.getAmount() + amount) <= item.getMaxStackSize()) {
                        tmp.setAmount(amount + tmp.getAmount());
                        amount = 0;
                        tmpInv[slot] = tmp;
                        break;
                    } else {
                        amount = amount - (item.getMaxStackSize() - tmp.getAmount());
                        tmp.setAmount(item.getMaxStackSize());
                        tmpInv[slot] = tmp;
                        continue;
                    }
                }
            }
            if (amount > 0) {
                for (int i = 0; i < tmpInv.length; i++) {
                    if (tmpInv[i] != null && tmpInv[i].getType() == Material.AIR) {
                        item.setAmount(amount);
                        tmpInv[i] = item.clone();
                        amount = 0;
                        break;
                    }
                }
            }
            if (amount > 0) {
                return false;
            }
        }
        for (int i = 0; i < tmpInv.length; i++) {
            if (tmpInv[i] != null && !tmpInv[i].equals(inventory.getItem(i))) {
                inventory.setItem(i, tmpInv[i]);
            }
        }
        return true;
    }

    public static boolean removeItem(Player player, ItemStack item, int amount) {
        return removeItem(player.getInventory(), item, amount);
    }

    public static boolean removeItem(Inventory inventory, ItemStack item, int amount) {
        ItemStack[] items = new ItemStack[inventory.getSize()];
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null &&
                    inventory.getItem(i).getType() != Material.AIR) {
                items[i] = inventory.getItem(i).clone();
            } else {
                items[i] = new ItemStack(Material.AIR);
            }
        }
        boolean success = false;
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack tmp = items[slot];
            if (tmp != null && tmp.isSimilar(item) && tmp.getAmount() > 0) {
                if (tmp.getAmount() < amount) {
                    amount = amount - tmp.getAmount();
                    items[slot] = new ItemStack(Material.AIR);
                    continue;
                } else if (tmp.getAmount() > amount) {
                    tmp.setAmount(tmp.getAmount() - amount);
                    amount = 0;
                    success = true;
                    break;
                } else {
                    items[slot] = new ItemStack(Material.AIR);
                    amount = 0;
                    success = true;
                    break;
                }
            }
        }
        if (success) {
            for (int i = 0; i < items.length; i++) {
                if (!items[i].equals(inventory.getItem(i))) {
                    inventory.setItem(i, items[i]);
                }
            }
            return true;
        }
        return false;
    }

    public static int getAmount(Player p, ItemStack item) {
        return getAmount(p.getInventory(), item);
    }

    public static int getAmount(Inventory inventory, ItemStack item) {
        int amount = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null &&
                    inventory.getItem(i).getType() != Material.AIR &&
                    inventory.getItem(i).isSimilar(item)) {
                amount += inventory.getItem(i).getAmount();
            }
        }
        return amount;
    }

    public static boolean hasEnoughSpace(Player player, ItemStack item, int amount) {
        return hasEnoughSpace(player.getInventory(), item, amount);
    }

    public static boolean hasEnoughSpace(Inventory inventory, ItemStack item) {
        return hasEnoughSpace(inventory, item, item.getAmount());
    }

    public static boolean hasEnoughSpace(Inventory inventory, ItemStack item, int amount) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= 36 && i <= 39 && inventory instanceof PlayerInventory) {
                continue;
            }
            if (inventory.getItem(i) != null && item.isSimilar(inventory.getItem(i)) &&
                    inventory.getItem(i).getAmount() < item.getMaxStackSize()) {
                amount -= item.getMaxStackSize() - inventory.getItem(i).getAmount();
            } else if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                amount = 0;
            }
            if (amount < 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove items from inventory.
     * Either all removed or none removed.
     *
     * @param inv           the inventory
     * @param itemToBeTaken items to be removed
     * @return If null, then all designated items are removed. If not null, it contains the items missing
     */
    public static List<ItemStack> withdrawInventoryAtomic(Inventory inv, List<ItemStack> itemToBeTaken) {
        ItemStack[] itemStacks = inv.getContents();
        ItemStack[] cloneStacks = new ItemStack[itemStacks.length];
        for (int i = 0; i < itemStacks.length; i++) {
            cloneStacks[i] = itemStacks[i] == null ? null : itemStacks[i].clone();
        }

        List<ItemStack> ret = new ArrayList<>();

        for (ItemStack item : itemToBeTaken) {
            int sizeReq = item.getAmount();

            for (int i = 0; i < cloneStacks.length; i++) {
                if (cloneStacks[i] == null) continue;
                if (cloneStacks[i].isSimilar(item)) {
                    int sizeSupp = cloneStacks[i].getAmount();
                    if (sizeSupp > sizeReq) {
                        cloneStacks[i].setAmount(sizeSupp - sizeReq);
                        sizeReq = 0;
                        break;
                    } else {
                        cloneStacks[i] = null;
                        sizeReq -= sizeSupp;
                        if (sizeReq == 0) break;
                    }
                }
            }

            if (sizeReq > 0) {
                ItemStack n = item.clone();
                item.setAmount(sizeReq);
                ret.add(n);
            }
        }

        if (ret.size() == 0) {
            inv.setContents(cloneStacks);
            return null;
        } else {
            return ret;
        }
    }
}
