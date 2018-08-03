package cat.nyaa.nyaacore;

import org.librazy.nclangchecker.LangKey;

public interface ILocalizer {
    /**
     * Get the language item then format with `para` by {@link String#format(String, Object...)}
     */
    String getFormatted(@LangKey String key, Object... para);

    boolean hasKey(@LangKey String key);
}
