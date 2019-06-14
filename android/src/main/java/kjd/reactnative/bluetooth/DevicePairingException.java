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

    public DevicePairingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
