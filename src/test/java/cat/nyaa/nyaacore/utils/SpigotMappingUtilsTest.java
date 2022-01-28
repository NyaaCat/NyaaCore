package cat.nyaa.nyaacore.utils;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import cat.nyaa.nyaacore.NyaaCoreLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.logging.Logger;

public class SpigotMappingUtilsTest {
    private ServerMock server;
    private NyaaCoreLoader plugin;

    @Before
    public void setUp()
    {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(NyaaCoreLoader.class);
    }
    @After
    public void tearDown()
    {
        MockBukkit.unmock();
    }


    @Test
    public void getObfuscatedFieldNameOptionalTest() {
        SpigotMappingUtils.init(plugin);
        Optional<String> optional =  SpigotMappingUtils.getSimpleObfuscatedFieldNameOptional("net/minecraft/world/entity/player/EntityHuman","DATA_PLAYER_MODE_CUSTOMISATION",null);
        assert (optional.isPresent());
        //Logger.getAnonymousLogger().info(optional.get());
    }
}
