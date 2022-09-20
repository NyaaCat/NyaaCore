package cat.nyaa.nyaacore.cmdreceiver.dispatchtest;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public class CmdSub2A extends CommandReceiver {
    public CmdSub2A(Plugin plugin, ILocalizer i18n) {
        super(plugin, i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "sub2.def";
    }

    // call with: nct sub2 {[anything except "b" and "c"] ...}
    @SubCommand(isDefaultCommand = true)
    public void sub2ADef(CommandSender sender, Arguments args) {
        DispatchTest.callback.onCommand("nct-sub2-a-<def>", sender, args);
    }

    // call with: nct sub2 c {...}
    @SubCommand(value = "c", tabCompleter = "ctc")
    public void sub2AC(CommandSender sender, Arguments args) {
        DispatchTest.callback.onCommand("nct-sub2-a-c", sender, args);
    }

    // no way to call this sub command
    @SubCommand(value = "b")
    public void sub2AB(CommandSender sender, Arguments args) {
        Assertions.fail("should not be able to be called");
    }

    public List<String> ctc(CommandSender sender, Arguments args) {
        DispatchTest.callback.onTab("nct-sub2a-ctc", sender, args);
        String s = args.next();
        while (args.top() != null) s = args.next();
        List<String> ret = new ArrayList<>();
        ret.add(s + "_s1");
        return ret;
    }
}
