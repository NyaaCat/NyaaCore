package cat.nyaa.nyaacore.utils;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class MathUtils {
    public static final int MILLI_IN_SEC = 1000;
    private static final Random rng = new Random();

    /**
     * Generate a random number in [min,max]
     */
    public static int uniformRangeInclusive(int minInclusive, int maxInclusive) {
        if (maxInclusive < minInclusive) throw new IllegalArgumentException();
        return rng.nextInt(maxInclusive - minInclusive + 1) + minInclusive;
    }

    /**
     * Select n from candidates. Return all candidates if n &gt; candidates.size()
     * Order is not preserved.
     */
    public static <T> ImmutableList<T> randomSelect(List<T> candidates, int n) {
        if (candidates == null || n < 0) throw new IllegalArgumentException();
        if (n > candidates.size()) n = candidates.size();
        if (n == 0) return ImmutableList.of();
        if (n == candidates.size()) return ImmutableList.copyOf(candidates);
        List<T> clone = new ArrayList<>(candidates);
        Collections.shuffle(clone, rng);

        ImmutableList.Builder<T> b = ImmutableList.builder();
        for (int i = 0; i < n; i++) b.add(clone.get(i));
        return b.build();
    }
}
