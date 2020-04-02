package kjd.reactnative.bluetooth;

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
