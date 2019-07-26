package cat.nyaa.nyaacore.cmdreceiver;

public class BadCommandException extends RuntimeException {
    public final Object[] objs;

    public BadCommandException() {
        super("");
        objs = null;
    }

    /**
     * show formatted error message to player
     *
     * @param msg_internal msg template key. e.g. `internal.warn.***'
     * @param args         arguments
     */
    public BadCommandException(String msg_internal, Object... args) {
        super(msg_internal);
        objs = args;
    }

    public BadCommandException(String msg_internal, Throwable cause, Object... args) {
        super(msg_internal, cause);
        objs = args;
    }
}
