package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;

import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Handles events created from a Bluetooth device.  The device
 */
public interface BluetoothDeviceListener {

    /**
     * Data received from the {@link BluetoothDevice}.
     *
     * @param device device from which this device originated
     * @param receivedData
     * @return
     */
    void onReceivedData(NativeDevice device, String receivedData);

    /**
     * A new connection has been successfully made.
     *
     * @param device
     */
    void onConnectionSuccess(NativeDevice device);

    /**
     * A new connection has failed.
     *
     * @param device
     * @param reason
     */
    void onConnectionFailed(NativeDevice device, Throwable reason);

    /**
     * Connection was lost.
     *
     * @param device
     * @param reason
     */
    void onConnectionLost(NativeDevice device, Throwable reason);

    /**
     * Handle a generic error.
     *
     * @param reason
     */
    void onError(NativeDevice device, Throwable reason);
}
