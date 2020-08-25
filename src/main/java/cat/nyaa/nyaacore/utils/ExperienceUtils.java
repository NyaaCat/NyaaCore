package cat.nyaa.nyaacore.utils;

import com.google.common.primitives.Ints;
import org.bukkit.entity.Player;

public final class ExperienceUtils {
    /**
     * How much exp points at least needed to reach this level.
     * i.e. getLevel() = level &amp;&amp; getExp() == 0
     */
    public static int getExpForLevel(int level) {
        if (level < 0) throw new IllegalArgumentException();
        else if (level <= 16) return (level + 6) * level;
        else if (level < 32) return Ints.checkedCast(Math.round(2.5 * level * level - 40.5 * level + 360));
        else return Ints.checkedCast(Math.round(4.5 * level * level - 162.5 * level + 2220));
    }

    /**
     * The true exp point for a player at this time.
     */
    public static int getExpPoints(Player p) {
        int pointForCurrentLevel = Math.round(p.getExpToLevel() * p.getExp());
        return getExpForLevel(p.getLevel()) + pointForCurrentLevel;
    }

    public static void subtractExpPoints(Player p, int points) {
        if (points < 0) throw new IllegalArgumentException();
        if (points == 0) return;
        int total = getExpPoints(p);
        if (total < points) throw new IllegalArgumentException("Negative Exp Left");
        int newLevel = getLevelForExp(total - points);
        int remPoint = total - points - getExpForLevel(newLevel);
        p.setLevel(newLevel);
        p.setExp(0);
        p.giveExp(remPoint);
    }

    /**
     * Which level the player at if he/she has this mount of exp points
     * TODO optimization
     */
    public static int getLevelForExp(int exp) {
        if (exp < 0) throw new IllegalArgumentException();
        for (int lv = 1; lv < 21000; lv++) {
            if (getExpForLevel(lv) > exp) return lv - 1;
        }
        throw new IllegalArgumentException("exp too large");
    }

    /**
     * Change the player's experience (not experience level)
     * Related events may be triggered.
     *
     * @param p   the target player
     * @param exp amount of xp to be added to the player,
     *            if negative, then subtract from the player.
     * @throws IllegalArgumentException if the player ended with negative xp
     */
    public static void addPlayerExperience(Player p, int exp) {
        if (exp > 0) {
            p.giveExp(exp);
        } else if (exp < 0) {
            subtractExpPoints(p, -exp);
        }
    }
}
