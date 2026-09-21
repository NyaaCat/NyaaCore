package cat.nyaa.nyaacore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Regression guard for the Minecraft 26.2 port.
 *
 * NyaaCoreLoader used to read the server version through
 * {@code net.minecraft.SharedConstants.getCurrentVersion()}. That single reference dragged
 * NMS into the one class every MockBukkit-based test has to load, which is why the
 * MockBukkit tests could only run with the Mojang-mapped server jar on the test classpath -
 * and that jar collides with MockBukkit's own META-INF/services entries.
 *
 * The loader now uses {@link org.bukkit.Bukkit#getMinecraftVersion()} instead. If somebody
 * reintroduces an NMS reference here, LanguageRepositoryTest / MessageTest / DispatchTest
 * would start failing with a confusing NoClassDefFoundError; fail loudly and specifically
 * instead.
 */
class NyaaCoreLoaderNmsDecouplingTest {

    private static byte[] bytecodeOf(final Class<?> type) throws Exception {
        final String resource = "/" + type.getName().replace('.', '/') + ".class";
        try (InputStream in = type.getResourceAsStream(resource)) {
            assertNotNull(in, "cannot read bytecode of " + type);
            return in.readAllBytes();
        }
    }

    private static boolean referencesUtf8(final byte[] bytecode, final String needleText) {
        final byte[] needle = needleText.getBytes(StandardCharsets.UTF_8);
        outer:
        for (int i = 0; i + needle.length <= bytecode.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (bytecode[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    @Test
    void loaderDoesNotReferenceNms() throws Exception {
        final byte[] bytecode = bytecodeOf(NyaaCoreLoader.class);
        assertFalse(referencesUtf8(bytecode, "net/minecraft/"),
            "NyaaCoreLoader must not reference net.minecraft.* - it is loaded by every "
                + "MockBukkit test, which runs with paper-api only");
        assertFalse(referencesUtf8(bytecode, "org/bukkit/craftbukkit/"),
            "NyaaCoreLoader must not reference CraftBukkit internals");
    }

    @Test
    void loaderUsesThePaperApiVersionAccessor() throws Exception {
        final byte[] bytecode = bytecodeOf(NyaaCoreLoader.class);
        assertTrue(referencesUtf8(bytecode, "getMinecraftVersion"),
            "NyaaCoreLoader should report the server version via Bukkit#getMinecraftVersion()");
    }

    /**
     * The NMS-using utility classes are still expected to reference NMS - that is the whole
     * point of NyaaCore. Only the loader was decoupled.
     */
    @Test
    void nmsUtilsStillUsesMojangMappedNms() throws Exception {
        assertTrue(referencesUtf8(bytecodeOf(cat.nyaa.nyaacore.utils.NmsUtils.class), "net/minecraft/"),
            "NmsUtils lost its NMS implementation");
        assertTrue(referencesUtf8(bytecodeOf(cat.nyaa.nyaacore.utils.ItemTagUtils.class),
                "net/minecraft/core/component/DataComponents"),
            "ItemTagUtils no longer uses the 1.20.5+ DataComponents API");
    }
}
