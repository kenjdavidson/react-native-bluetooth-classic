package kjd.reactnative.bluetooth;

public enum BTRequest {
    ENABLE_BLUETOOTH(1),
    PAIR_DEVICE(2);

    public final int code;
    private BTRequest(int code) {
        this.code = code;
    }
}
