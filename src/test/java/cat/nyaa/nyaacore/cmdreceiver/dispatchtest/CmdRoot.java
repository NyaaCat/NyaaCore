package cat.nyaa.nyaacore.cmdreceiver.dispatchtest;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CmdRoot extends CommandReceiver {
    public CmdRoot(Plugin plugin) {
        super(plugin, null);
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
        DispatchTest.callback.onCommand("nct-sub3", sender, args);
    }

    public List<String> sub3tc(CommandSender sender, Arguments args) {
        DispatchTest.callback.onTab("nct-sub3tc", sender, args);
        String s = args.next();
        while (args.top() != null) s = args.next();
        List<String> ret = new ArrayList<>();
        ret.add(s + "_s1");
        return ret;
    }

    // call with: nct {[anything except sub123] ...}
    @SubCommand(isDefaultCommand = true, tabCompleter = "deftc")
    public void defCmd(CommandSender sender, Arguments args) {
        DispatchTest.callback.onCommand("nct-<def>", sender, args);
    }

    public List<String> deftc(CommandSender sender, Arguments args) {
        DispatchTest.callback.onTab("nct-deftc", sender, args);
        String s = args.next();
        while (args.top() != null) s = args.next();
        List<String> ret = new ArrayList<>();
        ret.add(s + "_s1");
        return ret;
    }
}
