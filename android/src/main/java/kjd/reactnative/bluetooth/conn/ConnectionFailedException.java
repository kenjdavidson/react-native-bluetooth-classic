package kjd.reactnative.bluetooth.conn;

import kjd.reactnative.bluetooth.BluetoothException;
import kjd.reactnative.bluetooth.Exceptions;
import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Connection failed.
 *
 * @author kendavidson
 */
public class ConnectionFailedException extends BluetoothException {

    public ConnectionFailedException(NativeDevice device, Throwable error) {
        super(device,
                Exceptions.CONNECTION_FAILED.message(device.getAddress()),
                error);
    }
}
