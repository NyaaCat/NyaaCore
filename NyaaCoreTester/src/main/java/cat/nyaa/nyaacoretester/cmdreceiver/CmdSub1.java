package cat.nyaa.nyaacoretester.cmdreceiver;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CmdSub1 extends CommandReceiver {
    public CmdSub1(NyaaCoreTester plugin, ILocalizer i18n) {
        super(plugin, i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "sub1";
    }

    // call with: nct sub1 a {...}
    @SubCommand("a")
    public void sub3Cmd(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub1-a", args);
    }

    // call with: nct sub1 {[anything except "a"] ...}
    @SubCommand(isDefaultCommand = true, tabCompleter = "deftc")
    public void sub1Def(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub1-<def>", args);
    }

    public List<String> deftc(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub1tc", args);
        String s = args.next();
        while (args.top() != null) s = args.next();
        List<String> ret = new ArrayList<>();
        ret.add(s + "_s1");
        return ret;
    }
}
