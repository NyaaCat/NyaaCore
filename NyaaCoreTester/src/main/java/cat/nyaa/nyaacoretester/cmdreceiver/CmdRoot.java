package cat.nyaa.nyaacoretester.cmdreceiver;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.DefaultCommand;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

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
    @SubCommand("sub3")
    public void sub3Cmd(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub3", args);
    }

    // call with: nct {[anything except sub123] ...}
    @DefaultCommand
    public void defCmd(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-<def>", args);
    }
}
