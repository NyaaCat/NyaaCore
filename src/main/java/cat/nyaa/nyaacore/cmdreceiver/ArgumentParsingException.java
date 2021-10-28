package cat.nyaa.nyaacore.cmdreceiver;

public class ArgumentParsingException extends Exception {
    public String cmdline;
    public int index;
    public String reason;

    public ArgumentParsingException(String cmdline, int index, String reason) {
        this.cmdline = cmdline;
        this.index = index;
        this.reason = reason;
    }
}
