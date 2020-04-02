package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Handle {@link BluetoothDevice} connection events - primarily
 * used for Connection and Accept requests.
 *
 * @author kendavidson
 */
public interface ConnectionCallback {

    /**
     * Indicates that a connection was successful.  This can be used for managing requests
     * for connecting as a server (accept) and connecting as client (connect).
     *
     * @param device
     * @param socket
     */
    void onConnectionSuccess(BluetoothDevice device, BluetoothSocket socket);

    /**
     * Indicates that a connection failure has occurred.  A connection failure does not
     * necessarily mean that the device was disconnected.  If an error and disconnect occurs
     * then both callbacks will be fired.
     *
     * @param device
     * @param e
     */
    void onConnectionFailure(BluetoothDevice device, Throwable e);

    /**
     * Indicates that the device was disconnected.
     *
     * @param device
     */
    void onDisconnect(BluetoothDevice device);

}
