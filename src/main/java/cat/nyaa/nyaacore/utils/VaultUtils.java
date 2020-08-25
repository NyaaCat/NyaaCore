package cat.nyaa.nyaacore.utils;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultUtils {
    private static Economy eco = null;
    private static Chat chat = null;

    public static Economy getVaultEconomy() {
        if (eco == null) {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (provider != null) {
                return eco = provider.getProvider();
            } else {
                throw new RuntimeException("Vault Error: No EconomyProvider found");
            }
        } else {
            return eco;
        }
    }

    public static Chat getVaultChat() {
        if (chat == null) {
            RegisteredServiceProvider<Chat> provider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
            if (provider != null) {
                return chat = provider.getProvider();
            } else {
                throw new RuntimeException("Vault Error: No ChatProvider found");
            }
        } else {
            return chat;
        }
    }

    public static double balance(OfflinePlayer p) {
        return getVaultEconomy().getBalance(p);
    }

    public static boolean enoughMoney(OfflinePlayer p, long money) {
        return money <= balance(p);
    }

    public static boolean enoughMoney(OfflinePlayer p, double money) {
        return money <= balance(p);
    }

    public static boolean withdraw(OfflinePlayer p, long money) {
        EconomyResponse rsp = getVaultEconomy().withdrawPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public static boolean withdraw(OfflinePlayer p, double money) {
        EconomyResponse rsp = getVaultEconomy().withdrawPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public static boolean deposit(OfflinePlayer p, long money) {
        EconomyResponse rsp = getVaultEconomy().depositPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public static boolean deposit(OfflinePlayer p, double money) {
        EconomyResponse rsp = getVaultEconomy().depositPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public static String getPlayerPrefix(Player player) {
        return getVaultChat().getPlayerPrefix(player);
    }

    public static void setPlayerPrefix(Player player, String prefix, boolean hasPEX) {
        if (hasPEX) {
            getVaultChat().setPlayerPrefix(null, player, prefix);
        } else {
            getVaultChat().setPlayerPrefix(player, prefix);
        }
    }

    public static String getPlayerSuffix(Player player) {
        return getVaultChat().getPlayerSuffix(player);
    }

    public static void setPlayerSuffix(Player player, String suffix, boolean hasPEX) {
        if (hasPEX) {
            getVaultChat().setPlayerSuffix(null, player, suffix);
        } else {
            getVaultChat().setPlayerSuffix(player, suffix);
        }
    }
}
