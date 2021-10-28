package cat.nyaa.nyaacore.cmdreceiver.dispatchtest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import cat.nyaa.nyaacore.NyaaCoreLoader;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DispatchTest {
    public interface ICallback {
        void onCommand(String mark, CommandSender sender, Arguments args);

        void onTab(String mark, CommandSender sender, Arguments args);
    }

    public static ICallback callback;

    private ServerMock server;
    private NyaaCoreLoader plugin;
    private CmdRoot cmdRoot;
    private PluginCommand cmd;


    @Before
    public void setUp() {
        callback = Mockito.mock(ICallback.class);
        server = MockBukkit.mock();
        plugin = MockBukkit.load(NyaaCoreLoader.class);
        cmdRoot = new CmdRoot(plugin);

        cmd = PluginCommandUtils.createPluginCommand("nct", plugin);
        cmd.setExecutor(cmdRoot);
        server.getCommandMap().register("nyaacoretest", cmd);
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    private Arguments invoke(String command, String mark) {
        CommandSender sender = server.getConsoleSender();
        server.dispatchCommand(sender, command);
        ArgumentCaptor<Arguments> captor = ArgumentCaptor.forClass(Arguments.class);
        verify(callback).onCommand(eq(mark), same(sender), captor.capture());
        return captor.getValue();
    }

    private List<String> tabCompletion(String command, String mark) {
        String[] split = command.split(" ", -1);
        String alias = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        CommandSender sender = server.getConsoleSender();
        List<String> result = cmdRoot.onTabComplete(sender, cmd, alias, args);

        ArgumentCaptor<Arguments> captor = ArgumentCaptor.forClass(Arguments.class);
        verify(callback).onTab(eq(mark), same(sender), captor.capture());
        assertNull(captor.getValue().next());
        return result;
    }

    public static boolean equalsIgnoreOrder(List<String> a, List<String> b) {
        if (a.size() != b.size()) return false;
        List<String> a2 = new ArrayList<>(a);
        List<String> b2 = new ArrayList<>(b);
        Collections.sort(a2);
        Collections.sort(b2);
        return a2.equals(b2);
    }

    @Test
    public void testRootDef() {
        Arguments args = invoke("nct", "nct-<def>");
        assertNull(args.next());
    }

    @Test
    public void testRootDefExtraArgs() {
        Arguments args = invoke("nct abcd", "nct-<def>");
        assertEquals("abcd", args.next());
        assertNull(args.next());
    }

    @Test
    public void testRootSub3() {
        Arguments args = invoke("nct sub3", "nct-sub3");
        assertNull(args.next());
    }

    @Test
    public void testRootSub1A() {
        Arguments args = invoke("nct sub1 a", "nct-sub1-a");
        assertNull(args.next());
    }

    @Test
    public void testRootSub1Def() {
        Arguments args = invoke("nct sub1", "nct-sub1-<def>");
        assertNull(args.next());
    }

    @Test
    public void testRootSub1DefExtraArgs() {
        Arguments args = invoke("nct sub1 abcd", "nct-sub1-<def>");
        assertEquals("abcd", args.next());
        assertNull(args.next());
    }


    @Test
    public void testRootSub2B() {
        Arguments args = invoke("nct sub2 b", "nct-sub2-b");
        assertNull(args.next());
    }

    @Test
    public void testRootSub2ADef() {
        Arguments args = invoke("nct sub2", "nct-sub2-a-<def>");
        assertNull(args.next());
    }

    @Test
    public void testRootSub2ADefExtraArgs() {
        Arguments args = invoke("nct sub2 abcd", "nct-sub2-a-<def>");
        assertEquals("abcd", args.next());
        assertNull(args.next());
    }

    @Test
    public void testRootSub2AC() {
        Arguments args = invoke("nct sub2 c", "nct-sub2-a-c");
        assertNull(args.next());
    }

    @Test
    public void testTabSub3() {
        List<String> result = tabCompletion("nct sub3 a", "nct-sub3tc");
        assertTrue(equalsIgnoreOrder(result, List.of("a_s1")));
    }

    @Test
    public void testTabDef() {
        List<String> result = tabCompletion("nct sub", "nct-deftc");
        assertTrue(equalsIgnoreOrder(result, List.of("sub1", "sub2", "sub3", "sub_s1")));
    }

    @Test
    public void testTabSub1() {
        List<String> result = tabCompletion("nct sub1 ", "nct-sub1tc");
        assertTrue(equalsIgnoreOrder(result, List.of("_s1", "a", "help")));
    }

    @Test
    public void testTabSub2B() {
        List<String> result = tabCompletion("nct sub2 b abcd", "nct-sub2-btc");
        assertTrue(equalsIgnoreOrder(result, List.of("abcd_s1")));
    }

    @Test
    public void testTabSub2AC() {
        List<String> result = tabCompletion("nct sub2 c abcd", "nct-sub2a-ctc");
        assertTrue(equalsIgnoreOrder(result, List.of("abcd_s1")));
    }
}

