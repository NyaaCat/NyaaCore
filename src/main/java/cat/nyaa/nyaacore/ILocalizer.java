package cat.nyaa.nyaacore;

public interface ILocalizer {
    /**
     * Get the language item then format with `para` by {@link String#format(String, Object...)}
     */
    String getFormatted(String key, Object... para);

    boolean hasKey(String key);
}
