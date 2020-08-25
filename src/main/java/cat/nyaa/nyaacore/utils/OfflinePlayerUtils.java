package cat.nyaa.nyaacore.utils;

import cat.nyaa.nyaacore.NyaaCoreLoader;
import cat.nyaa.nyaacore.http.client.HttpClient;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OfflinePlayerUtils {
    private static final String UNDASHED = "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})";
    private static final String DASHED = "$1-$2-$3-$4-$5";
    private static final TypeToken<List<Map<String, Object>>> typeTokenListMap = new TypeToken<List<Map<String, Object>>>() {
    };
    private static ConcurrentMap<String, OfflinePlayer> playerCache;
    private static ConcurrentMap<UUID, String> nameCache;

    private OfflinePlayerUtils() {
    }

    public static void init() {
        playerCache = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null)
                .collect(Collectors.toConcurrentMap(p -> p.getName().toLowerCase(Locale.ENGLISH), Function.identity(), BinaryOperator.maxBy(Comparator.comparing(OfflinePlayer::getLastPlayed))));
        nameCache = playerCache.entrySet().stream().collect(Collectors.toConcurrentMap(e -> e.getValue().getUniqueId(), Map.Entry::getKey));
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
        CompletableFuture<FullHttpResponse> response = HttpClient.postJson("https://api.mojang.com/profiles/minecraft", Collections.emptyMap(), new Gson().toJson(Stream.of(names).collect(Collectors.toList())));
        return response.thenApply((r) -> {
            try {
                NyaaCoreLoader.getInstance().getLogger().log(Level.FINER, "request name -> uuid api " + r.status().code());
                if (r.status().code() > 299 || r.content() == null) {
                    return HashBiMap.<UUID, String>create();
                }
                List<Map<String, Object>> result = new Gson().fromJson(r.content().toString(UTF_8), typeTokenListMap.getType());
                return result.stream().collect(ImmutableBiMap.toImmutableBiMap(m -> {
                            m.get("id");
                            return UUID.fromString(((String) m.get("id")).replaceAll(UNDASHED, DASHED));
                        },
                        m -> (String) m.get("name")));
            } finally {
                ReferenceCountUtil.release(r);
            }
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
        CompletableFuture<FullHttpResponse> response = HttpClient.get("https://api.mojang.com/user/profiles/" + uuid.toString().toLowerCase().replace("-", "") + "/names", Collections.emptyMap());
        return response.thenApply((r) -> {
            try {
                NyaaCoreLoader.getInstance().getLogger().log(Level.FINER, "request uuid -> name api " + r.status().code());
                if (r.status().code() > 299 || r.content() == null) {
                    return null;
                }
                List<Map<String, Object>> nameMapsList = new Gson().fromJson(r.content().toString(UTF_8), typeTokenListMap.getType());
                if (nameMapsList.isEmpty()) {
                    return null;
                }
                nameCache.put(uuid, nameMapsList.get(nameMapsList.size() - 1).get("name").toString());
                return nameCache.get(uuid);
            } finally {
                ReferenceCountUtil.release(r);
            }
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
