package kjd.reactnative.bluetooth;

public class DevicePairingException extends RuntimeException {

    public DevicePairingException() {
    }

    public DevicePairingException(String message) {
        super(message);
    }

    public DevicePairingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DevicePairingException(Throwable cause) {
        super(cause);
    }

}
