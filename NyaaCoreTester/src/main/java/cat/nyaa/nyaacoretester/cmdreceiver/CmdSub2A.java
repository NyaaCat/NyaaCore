package cat.nyaa.nyaacoretester.cmdreceiver;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.bukkit.command.CommandSender;
import org.junit.Assert;

public class CmdSub2A extends CommandReceiver {
    public CmdSub2A(NyaaCoreTester plugin, ILocalizer i18n) {
        super(plugin, i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "sub2.def";
    }

    // call with: nct sub2 {[anything except "b" and "c"] ...}
    @SubCommand(isDefaultCommand = true)
    public void sub2ADef(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub2-a-<def>", args);
    }

    // call with: nct sub2 c {...}
    @SubCommand("c")
    public void sub2AC(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub2-a-c", args);
    }

    // no way to call this sub command
    @SubCommand("b")
    public void sub2AB(CommandSender sender, Arguments args) {
        Assert.fail("should not be able to be called");
    }
}
