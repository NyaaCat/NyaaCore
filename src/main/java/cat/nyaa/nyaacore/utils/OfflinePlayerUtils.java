package cat.nyaa.nyaacore.utils;

import cat.nyaa.nyaacore.NyaaCoreLoader;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class OfflinePlayerUtils {
    private static final String UNDASHED = "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})";
    private static final String DASHED = "$1-$2-$3-$4-$5";
    private static final TypeToken<List<Map<String, Object>>> typeTokenListMap =
            new TypeToken<List<Map<String, Object>>>() {
            };
    private static ConcurrentMap<String, OfflinePlayer> playerCache;
    private static ConcurrentMap<UUID, String> nameCache;

    private OfflinePlayerUtils() {
    }

    public static void init() {
        playerCache = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null)
                .collect(Collectors.toConcurrentMap(p -> p.getName().toLowerCase(Locale.ENGLISH), Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(OfflinePlayer::getLastPlayed))));
        nameCache = playerCache.entrySet().stream().collect(
                Collectors.toConcurrentMap(e -> e.getValue().getUniqueId(), Map.Entry::getKey));
    }

    public static OfflinePlayer lookupPlayer(String name) {
        OfflinePlayer player = Bukkit.getPlayerExact(name);
        if (player != null) return player;
        return playerCache.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static CompletableFuture<BiMap<String, UUID>> lookupPlayerNamesOnline(String... names) {
        List<String> nameList = new LinkedList<>(Arrays.asList(names));
        BiMap<String, UUID> ret = HashBiMap.create();
        Iterator<String> iterator = nameList.iterator();
        while (iterator.hasNext()) {
            String n = iterator.next();
            OfflinePlayer player = playerCache.get(n.toLowerCase(Locale.ENGLISH));
            if (player != null) {
                iterator.remove();
                ret.put(n, player.getUniqueId());
            }
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/profiles/minecraft"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(List.of(names))))
                .build();
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply((response) -> {
                    NyaaCoreLoader.getInstance().getLogger().log(Level.FINER,
                            "request name -> uuid api " + response.statusCode());
                    if (response.statusCode() > 299 || response.body() == null) {
                        return HashBiMap.<UUID, String>create();
                    }
                    List<Map<String, Object>> result =
                            new Gson().fromJson(response.body(), typeTokenListMap.getType());
                    return result.stream().collect(ImmutableBiMap.toImmutableBiMap(m -> {
                                m.get("id");
                                return UUID.fromString(((String) m.get("id")).replaceAll(UNDASHED, DASHED));
                            },
                            m -> (String) m.get("name")));
                }).thenApply(u -> {
                    nameCache.putAll(u);
                    ret.putAll(u.inverse());
                    return ret;
                }).exceptionally((e) -> {
                    NyaaCoreLoader.getInstance().getLogger().log(Level.INFO, "failed to request name -> uuid api", e);
                    return ret;
                });
    }

    public static CompletableFuture<String> lookupPlayerNameByUuidOnline(UUID uuid) {
        String s = nameCache.get(uuid);
        if (s != null) {
            return CompletableFuture.completedFuture(s);
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.mojang.com/user/profiles/"
                                + uuid.toString().toLowerCase().replace("-", "")
                                + "/names"))
                .GET()
                .build();
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply((r) -> {
            NyaaCoreLoader.getInstance().getLogger().log(Level.FINER,
                    "request uuid -> name api " + r.statusCode());
            if (r.statusCode() > 299 || r.body() == null) {
                return null;
            }
            List<Map<String, Object>> nameMapsList =
                    new Gson().fromJson(r.body(), typeTokenListMap.getType());
            if (nameMapsList.isEmpty()) {
                return null;
            }
            nameCache.put(uuid, nameMapsList.get(nameMapsList.size() - 1).get("name").toString());
            return nameCache.get(uuid);
        }).exceptionally((e) -> {
            NyaaCoreLoader.getInstance().getLogger().log(Level.INFO, "failed to request uuid -> name api", e);
            return null;
        });
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
