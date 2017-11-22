package cat.nyaa.nyaacore.component;

import java.util.Map;
import java.util.UUID;

/**
 * Damage statistic is introduced by NyaaUtils
 * which tracks how much damage does every player make.
 * The implementation should track projectiles back to the shooter (arrow, potion, etc.)
 * <p>
 * See https://github.com/NyaaCat/NyaaUtils/blob/master/src/main/java/cat/nyaa/nyaautils/DamageStatListener.java
 */
public interface IDamageStatistic {
    /**
     * Find all attackers who attacked the victim.
     *
     * @param victim the victim
     * @return Map[playerUUID, totalDamageMade]
     */
    Map<UUID, Double> getDamageSources(UUID victim);

    /**
     * Find all victims of a player.
     *
     * @param playerId the player
     * @return Map[entityId, totalDamageMade]
     */
    Map<UUID, Double> getDamageVictims(UUID playerId);

    /**
     * Find out who healed this entity
     *
     * @param healee entityId
     * @return Map[playerUUID, totalHealingMade]
     */
    Map<UUID, Double> getHealers(UUID healee);

    /**
     * Find all entities the player healed
     *
     * @param healer playerUUID
     * @return Map[entityId, totalHealingMade]
     */
    Map<UUID, Double> getHealees(UUID healer);
}
