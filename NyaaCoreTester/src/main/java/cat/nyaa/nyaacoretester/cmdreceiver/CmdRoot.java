package cat.nyaa.nyaacoretester.cmdreceiver;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CmdRoot extends CommandReceiver {
    public CmdRoot() {
        super(NyaaCoreTester.instance, null);
    }


    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand("sub1")
    public CmdSub1 sub1;
    @SubCommand("sub2")
    public CmdSub2 sub2;

    // call with: nct sub3 {...}
    @SubCommand(value = "sub3", tabCompleter = "sub3tc")
    public void sub3Cmd(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub3", args);
    }

    public List<String> sub3tc(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub3tc", args);
        String s = args.next();
        while (args.top() != null) s = args.next();
        List<String> ret = new ArrayList<>();
        ret.add(s + "_s1");
        return ret;
    }

    // call with: nct {[anything except sub123] ...}
    @SubCommand(isDefaultCommand = true, tabCompleter = "deftc")
    public void defCmd(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-<def>", args);
    }

    public List<String> deftc(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-deftc", args);
        String s = args.next();
        while (args.top() != null) s = args.next();
        List<String> ret = new ArrayList<>();
        ret.add(s + "_s1");
        return ret;
    }
}
