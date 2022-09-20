package cat.nyaa.nyaacore.cmdreceiver.dispatchtest;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CmdSub2 extends CommandReceiver {
    @SubCommand(isDefaultCommand = true)
    public CmdSub2A sub2a;

    public CmdSub2(Plugin plugin, ILocalizer i18n) {
        super(plugin, i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "sub2";
    }

    // call with: nct sub2 b {...}
    @SubCommand(value = "b", tabCompleter = "btc")
    public void sub3Cmd(CommandSender sender, Arguments args) {
        DispatchTest.callback.onCommand("nct-sub2-b", sender, args);
    }

    public List<String> btc(CommandSender sender, Arguments args) {
        DispatchTest.callback.onTab("nct-sub2-btc", sender, args);
        String s = args.next();
        while (args.top() != null) s = args.next();
        List<String> ret = new ArrayList<>();
        ret.add(s + "_s1");
        return ret;
    }
}
