package cat.nyaa.nyaacore;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageTest {

    private ServerMock server;
    private NyaaCoreLoader plugin;
    public static final char COLOR_CHAR = '\u00A7';

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
    public void chatText() {
        PlayerMock pm = server.addPlayer();
        new Message("foobar").send(pm);
        pm.assertSaid(COLOR_CHAR+"f"+"foobar");
        //pm.assertSaid("foobar");
        pm.assertNoMoreSaid();
    }
}
