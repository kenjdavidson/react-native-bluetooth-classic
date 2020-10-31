package kjd.reactnative.bluetooth.conn;

import kjd.reactnative.bluetooth.BluetoothException;
import kjd.reactnative.bluetooth.Exceptions;
import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Connection lost.
 *
 * @author kendavidson
 */
public class ConnectionLostException extends BluetoothException {

    public ConnectionLostException(NativeDevice device, Throwable e) {
        super(device,
                Exceptions.CONNECTION_LOST.message(device.getAddress()),
                e);
    }
}
