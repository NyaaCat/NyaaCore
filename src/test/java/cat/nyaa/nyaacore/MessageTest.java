package cat.nyaa.nyaacore;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

public class MessageTest {

    public static final char COLOR_CHAR = '\u00A7';
    private ServerMock server;
    private NyaaCoreLoader plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(NyaaCoreLoader.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void chatText() {
        PlayerMock pm = server.addPlayer();
        new Message("foobar").send(pm);
        Assertions.assertEquals("foobar", pm.nextMessage());
        //pm.assertSaid("foobar");
        Assertions.assertNull(pm.nextComponentMessage());
    }
}
