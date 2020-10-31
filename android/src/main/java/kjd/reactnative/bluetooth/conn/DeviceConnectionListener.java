package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Callback reserved for connection requests.  This includes both <strong>connect</strong> as
 * client and <strong>accept</strong> as server.  In both cases, when a successful connection
 * is created the {@link BluetoothDevice} and a {@link BluetoothSocket} is made available to the
 * caller.  Similarly a failure returns a {@link BluetoothDevice} (this device on accept) and
 * the error which was caused the failure.
 *
 * @author kendavidson
 *
 */
public interface DeviceConnectionListener {

    /**
     * Indicates that a connection was successful.  This can be used for managing requests
     * for connecting as a server (accept) and connecting as client (connect).
     *
     * @param device
     */
    void onConnectionSuccess(NativeDevice device);

    /**
     * Indicates that a connection failure has occurred.  A connection failure does not
     * necessarily mean that the device was disconnected.  If an error and disconnect occurs
     * then both callbacks will be fired.
     *
     * @param device
     * @param e
     */
    void onConnectionFailure(NativeDevice device, Throwable e);

    /**
     * Handles when a connection is lost, this may or may not differ from the
     * {@link #onConnectionFailure(NativeDevice, Throwable)} but it's up to you.
     *
     * @param device
     * @param e
     */
    void onConnectionLost(NativeDevice device, Throwable e);

    /**
     * Handles errors that may not fit.
     *
     * @param device
     * @param e
     */
    void onError(NativeDevice device, Throwable e);
}
