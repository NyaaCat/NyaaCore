package cat.nyaa.nyaacoretester.cmdreceiver;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacoretester.NyaaCoreTester;
import org.bukkit.command.CommandSender;

public class CmdSub2 extends CommandReceiver {
    public CmdSub2(NyaaCoreTester plugin, ILocalizer i18n) {
        super(plugin, i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "sub2";
    }

    @SubCommand(isDefaultCommand = true)
    public CmdSub2A sub2a;

    // call with: nct sub2 b {...}
    @SubCommand("b")
    public void sub3Cmd(CommandSender sender, Arguments args) {
        CommandReceiverTest.touchMark("nct-sub2-b", args);
    }
}
