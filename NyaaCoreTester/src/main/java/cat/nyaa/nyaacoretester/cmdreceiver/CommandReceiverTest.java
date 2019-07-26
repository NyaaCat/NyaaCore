package cat.nyaa.nyaacoretester.cmdreceiver;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.bukkit.Bukkit;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandReceiverTest {
    private static String mark;
    private static Arguments args;
    public static void touchMark(String mark, Arguments args) {
        CommandReceiverTest.mark = mark;
        CommandReceiverTest.args = args;
    }

    public static void assertCommandInvoked(String command, String mark, Predicate<Arguments> argsAssertion) {
        CommandReceiverTest.mark = null;
        CommandReceiverTest.args = null;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        Assert.assertEquals(mark, CommandReceiverTest.mark);
        Assert.assertTrue(argsAssertion.test(CommandReceiverTest.args));
    }

    public static void assertTabCompletion(String command, String mark, String... completions) {
        assertTabCompletion(command, mark, args -> args.next() == null, completions);
    }

    public static void assertTabCompletion(String command, String mark, Predicate<Arguments> argsAssertion, String... completions) {
        String[] split = command.split(" ", -1);
        String alias = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);
        CommandReceiverTest.mark = null;
        CommandReceiverTest.args = null;
        List<String> result = NyaaCoreTester.instance.cmd.onTabComplete(Bukkit.getConsoleSender(), Bukkit.getPluginCommand("nyaacoretester"), alias, args);
        result = result == null ? Collections.emptyList() : result.stream().sorted().distinct().collect(Collectors.toList());
        Assert.assertEquals(mark, CommandReceiverTest.mark);
        Assert.assertTrue(argsAssertion.test(CommandReceiverTest.args));
        Assert.assertArrayEquals(completions, result.toArray());
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

    @Test
    public void testTabSub3() {
        assertTabCompletion("nct sub3 a", "nct-sub3tc", "a_s1");
    }

    @Test
    public void testTabDef() {
        assertTabCompletion("nct sub", "nct-deftc", "sub1", "sub2", "sub3", "sub_s1");
    }

    @Test
    public void testTabSub1() {
        assertTabCompletion("nct sub1 ", "nct-sub1tc", "_s1", "a", "help");
    }

    @Test
    public void testTabSub2B() {
        assertTabCompletion("nct sub2 b abcd", "nct-sub2-btc", "abcd_s1");
    }

    @Test
    public void testTabSub2AC() {
        assertTabCompletion("nct sub2 c abcd", "nct-sub2a-ctc", "abcd_s1");
    }
}
