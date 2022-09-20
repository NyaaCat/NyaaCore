package cat.nyaa.nyaacore;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.*;

public class LanguageRepositoryTest {

    @TempDir
    public static File another_plugin_data_dir;
    private static ServerMock server;
    private static NyaaCoreLoader nyaacore;
    private static Plugin plugin;

    @BeforeAll
    public static void setUpMockServer() throws IOException {
        server = MockBukkit.mock();
        nyaacore = MockBukkit.load(NyaaCoreLoader.class, true);
        plugin = Mockito.mock(Plugin.class);
        Mockito.when(plugin.getDataFolder()).thenReturn(another_plugin_data_dir);

        // Create file in NyaaCore's data folder
        FileWriter fw = new FileWriter(new File(nyaacore.getDataFolder(), "en_US.yml"));
        fw.write("internal:\n  info:\n    usage_prompt: usage_prompt_overwritten");
        fw.close();

        // Create file in plugin's data folder
        fw = new FileWriter(new File(plugin.getDataFolder(), "en_US.yml"));
        fw.write("key2: val2_overwritten");
        fw.close();
    }

    @AfterAll
    public static void tearDownMockServer() {
        MockBukkit.unmock();
    }

    @Test
    public void test() {
        LanguageRepository repo = new LanguageRepository() {
            @Override
            protected Plugin getPlugin() {
                return plugin;
            }

            @Override
            protected String getLanguage() {
                return "en_US";
            }
        };

        String s = "key: val\n";
        s += "key2: val2\n";
        s += "internal:\n";
        s += "  info:\n";
        s += "    using_language: using_lang_overwritten";
        InputStream bundled_lang_file = new ByteArrayInputStream(s.getBytes());
        Mockito.when(plugin.getResource(Mockito.eq("lang/en_US.yml"))).thenReturn(bundled_lang_file);
        repo.load();

        Assertions.assertEquals("Command Executed!", repo.getFormatted("internal.info.command_complete"));
        Assertions.assertEquals("usage_prompt_overwritten", repo.getFormatted("internal.info.usage_prompt"));
        Assertions.assertEquals("using_lang_overwritten", repo.getFormatted("internal.info.using_language"));
        Assertions.assertEquals("val", repo.getFormatted("key"));
        Assertions.assertEquals("val2_overwritten", repo.getFormatted("key2"));
    }
}
