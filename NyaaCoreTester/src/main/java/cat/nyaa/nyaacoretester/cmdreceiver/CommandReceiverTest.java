package cat.nyaa.nyaacoretester.cmdreceiver;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

public class CommandReceiverTest {
    private static CmdRoot cmd;
    private static String mark;
    private static Arguments args;
    public static void touchMark(String mark, Arguments args) {
        CommandReceiverTest.mark = mark;
        CommandReceiverTest.args = args;
    }

    public static void assertCommandInvoked(String command, String mark) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        Assert.assertEquals(mark, CommandReceiverTest.mark);
    }

    public static void assertCommandInvoked(String command, String mark, Predicate<Arguments> argsAssertion) {
        CommandReceiverTest.mark = null;
        CommandReceiverTest.args = null;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        Assert.assertEquals(mark, CommandReceiverTest.mark);
        Assert.assertTrue(argsAssertion.test(CommandReceiverTest.args));
    }

    @BeforeClass
    public static void setupCmd() {
        cmd = new CmdRoot();
    }

    @Test
    public void testRootDef() {
        assertCommandInvoked("nct", "nct-<def>", args->args.next() == null);
        assertCommandInvoked("nct abcd", "nct-<def>", args->{
            String s1 = args.next();
            return "abcd".equals(s1) && args.next() == null;
        });
    }

    @Test
    public void testRootSub3() {
        assertCommandInvoked("nct sub3", "nct-sub3", args->args.next() == null);
    }

    @Test
    public void testRootSub1A() {
        assertCommandInvoked("nct sub1 a", "nct-sub1-a", args->args.next() == null);
    }

    @Test
    public void testRootSub1Def() {
        assertCommandInvoked("nct sub1", "nct-sub1-<def>", args->args.next() == null);
        assertCommandInvoked("nct sub1 abcd", "nct-sub1-<def>", args->{
            String s1 = args.next();
            return "abcd".equals(s1) && args.next() == null;
        });
    }

    @Test
    public void testRootSub2B() {
        assertCommandInvoked("nct sub2 b", "nct-sub2-b",args->args.next() == null);
    }

    @Test
    public void testRootSub2ADef() {
        assertCommandInvoked("nct sub2", "nct-sub2-a-<def>",args->args.next() == null);
        assertCommandInvoked("nct sub2 abcd", "nct-sub2-a-<def>", args->{
            String s1 = args.next();
            return "abcd".equals(s1) && args.next() == null;
        });
    }

    @Test
    public void testRootSub2AC() {
        assertCommandInvoked("nct sub2 c", "nct-sub2-a-c",args->args.next() == null);
    }
}
