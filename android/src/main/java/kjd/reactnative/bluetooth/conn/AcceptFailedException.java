package kjd.reactnative.bluetooth.conn;

import kjd.reactnative.bluetooth.BluetoothException;

public class AcceptFailedException extends BluetoothException {
    public AcceptFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
