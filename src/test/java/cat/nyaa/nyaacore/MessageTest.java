package cat.nyaa.nyaacore;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageTest {

    public static final char COLOR_CHAR = '\u00A7';
    private ServerMock server;
    private NyaaCoreLoader plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(NyaaCoreLoader.class, true);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void chatText() {
        PlayerMock pm = server.addPlayer();
        new Message("foobar").send(pm);
        pm.assertSaid("foobar");
        //pm.assertSaid("foobar");
        pm.assertNoMoreSaid();
    }
}
