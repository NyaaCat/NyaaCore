package cat.nyaa.nyaacore.utils;

import org.junit.Test;

import java.util.Optional;

public class SpigotMappingUtilsTest {
    @Test
    public void getObfuscatedFieldNameOptionalTest() {
        Optional<String> optional = SpigotMappingUtils.getSimpleObfuscatedFieldNameOptional("net/minecraft/world/entity/player/EntityHuman", "DATA_PLAYER_MODE_CUSTOMISATION", null);
        assert (optional.isPresent());
    }
}
