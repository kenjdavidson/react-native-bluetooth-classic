package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.nio.ByteBuffer;

/**
 * Handles listening events from {@link android.bluetooth.BluetoothDevice} services.
 */
public interface BluetoothEventListener {

    /**
     * Data received from the {@link BluetoothDevice}.
     *
     * @param device
     * @param receivedData
     * @return
     */
    void onReceivedData(BluetoothDevice device, byte[] receivedData);

    /**
     * A new connection has been successfully made.
     *
     * @param device
     */
    void onConnectionSuccess(BluetoothDevice device);

    /**
     * A new connection has failed.
     *
     * @param device
     * @param reason
     */
    void onConnectionFailed(BluetoothDevice device, Throwable reason);

    /**
     * Connection was lost.
     *
     * @param device
     * @param reason
     */
    void onConnectionLost(BluetoothDevice device, Throwable reason);

    /**
     * Handle a general error.
     *
     * @param reason
     */
    void onError(Throwable reason);
}
