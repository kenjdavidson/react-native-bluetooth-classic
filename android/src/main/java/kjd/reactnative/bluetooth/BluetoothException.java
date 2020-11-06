package kjd.reactnative.bluetooth;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * General exception which is {@link Mappable} for the React Native bridge.   It provides the
 * {@link NativeDevice}, {@link RNBluetoothClassicModule} related error message
 * and the thrown exception.
 *
 * @author kendavidson
 */
public class BluetoothException extends RuntimeException implements Mappable {

    public static final String DEVICE = "device";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";

    private NativeDevice device;
    private String message;

    /**
     * Creates a new {@link BluetoothException}.
     *
     * @param message
     */
    public BluetoothException(String message) {
        this(null, message, null);
    }

    /**
     * Creates a new {@link BluetoothException}.
     *
     * @param message
     * @param cause
     */
    public BluetoothException(String message, Throwable cause) {
        this(null, message, cause);
    }

    /**
     * Creates a new {@link BluetoothException}.
     *
     * @param device
     * @param message
     * @param cause
     */
    public BluetoothException(NativeDevice device, String message, Throwable cause) {
        super(cause);
        this.device = device;
        this.message = message;
    }

    @Override
    public WritableMap map() {
        WritableMap map = Arguments.createMap();

        if (device != null)
            map.putMap(DEVICE, device.map());

        map.putString(ERROR, message);

        if (getCause() != null)
            map.putString(MESSAGE, getCause().getLocalizedMessage());

        return map;
    }
}
