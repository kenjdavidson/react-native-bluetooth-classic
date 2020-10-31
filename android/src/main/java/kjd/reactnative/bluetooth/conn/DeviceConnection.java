package kjd.reactnative.bluetooth.conn;

import java.io.IOException;
import java.util.Properties;

import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * {@link DeviceConnection} provides implemention details for a number of different connection
 * types.  Each connection type requires that the following are defined:
 * <ul>
 *     <li><strong>connect</strong> providing a {@link java.util.Properties} containing all the
 *     required information.  Missing arguments should throw {@link IllegalArgumentException}</li>
 *     otherwise the regular Bluetooth connection exceptions should be thrown.
 *     <li><strong>disconnect</strong> performs appropriate disconnection</li>
 *     <li><strong>available</strong> determines whether a manual read is available from the
 *     device.</li>
 *     <li><strong>read</strong> performs a manual read.  Automated reading is available in
 *     the form of event handling, in which case the {@link DeviceConnection} implementation is
 *     responsible for handling if required.</li>
 *     <li><strong>write</strong> performs a write.</li>
 * </ul>
 *
 * @author kdavidson
 *
 */
public interface DeviceConnection {

    /**
     * Retrieves the internal {@link NativeDevice} which is currently backing the
     * connection.
     *
     * @return
     */
    NativeDevice getDevice();

    /**
     * Connects to a device.
     *
     * @param device
     * @param properties
     * @param listener
     * @return
     */
    boolean connect(NativeDevice device,
                    Properties properties,
                    DeviceConnectionListener listener);

    /**
     * Disconnects from the currently connected {@link NativeDevice}.  It's up to the
     * implementation to determine whether exceptions should be thrown or state
     * should be checked.
     *
     * @return
     */
    boolean disconnect();

    /**
     * Writes the supplied data to the {@link NativeDevice} {@link java.io.OutputStream}.
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
    String read() throws IOException;

    /**
     * It's possible to connect to a device without a listener (manual reading) in order to switch
     * the connection a listener needs to be added.
     *
     * @param listener
     */
    boolean addDeviceListener(DataReceivedListener listener);

    /**
     * Removes the current listener.  Implementations can fail with an exception or a response.
     *
     *
     * @return
     */
    boolean removeDeviceListener();

    /**
     * Return the connection status.
     *
     * @return
     */
    ConnectionStatus getConnectionStatus();
}
