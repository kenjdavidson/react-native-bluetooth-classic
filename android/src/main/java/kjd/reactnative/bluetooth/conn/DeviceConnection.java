package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;

import kjd.reactnative.android.BiConsumer;
import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Provides a standardized {@link DeviceConnection} that the {@link kjd.reactnative.bluetooth.RNBluetoothClassicModule}
 * has the ability to create, customize and interact with.
 *
 * @author kdavidson
 *
 */
public interface DeviceConnection extends Runnable {

    /**
     * Get the current {@link BluetoothDevice} to which the {@link DeviceConnection}
     * is connected.
     *
     * @return
     */
    BluetoothDevice getDevice();

    /**
     * Disconnects from the currently connected {@link NativeDevice}.  It's up to the
     * implementation to determine whether exceptions should be thrown or state
     * should be checked.
     *
     * @return
     */
    boolean disconnect();

    /**
     * Writes the supplied data to the {@link NativeDevice}.
     * The inbound data is in a Base64 formatted String, as this was the original type
     * that was passed from React Native, implementations are responsible for converting
     * the Base64 String into the appropriate type - most of the forks are already
     * doing this, so it shouldn't be a problem.
     *
     * @param data
     */
    void write(byte[] data) throws IOException;

    /**
     * Provides the amount of data available for reading.  It's assume that a return
     * of 0 means that there is no data.
     *
     * @return
     */
    int available();

    /**
     * Clears the currently available data.
     *
     * @return
     */
    boolean clear();

    /**
     * Performs a manual read of the {@link NativeDevice} or backing Buffer.  Again
     * String(s) are used as the transfer data, although in this case there is no
     * requirement on Base64, you're free to perform any conversion from
     * original data as needed; as it will be the implementation decoding
     * it within React Native JS.
     *
     * @return
     */
    String read();

    /**
     * It's possible to connect to a device without a listener (manual reading) in order to switch
     * the connection a listener needs to be added.
     *
     * @param onDataReceived
     */
    void onDataReceived(BiConsumer<BluetoothDevice,String> onDataReceived);

    /**
     * Removes the current listener.  Implementations can fail with an exception or a response.
     *
     */
    void clearOnDataReceived();

    /**
     * Return the connection status.
     *
     * @return
     */
    ConnectionStatus getConnectionStatus();

    /**
     * Sets the onDisconnect handler for the {@link DeviceConnection}.  This will be called in
     * all instances of a disconnection: with an Exception on error, and with Null if requested
     * gracefully.
     *
     * @param onDisconnect
     */
    void onDisconnect(BiConsumer<BluetoothDevice,Exception> onDisconnect);

}
