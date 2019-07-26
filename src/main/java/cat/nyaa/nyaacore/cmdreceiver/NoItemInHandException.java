package cat.nyaa.nyaacore.cmdreceiver;

class NoItemInHandException extends RuntimeException {
    public final boolean isOffHand;

    /**
     * @param ifh true if require item in offhand
     */
    public NoItemInHandException(boolean ifh) {
        isOffHand = ifh;
    }
}
