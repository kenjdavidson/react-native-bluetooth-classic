package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

import kjd.reactnative.android.BiConsumer;


/**
 * Implements the comment features for a delimited device connection.  Delimited
 * connections are generally String based which parse / concatenate received messages
 * by their specified delimiter.  The standard mProperties required for this
 * connection are:
 * <ul>
 *     <li><strong>delimiter</strong> string delimited for parsing messages.</li>
 *     <li><strong>charset</strong> determines how bytes/Strings are parsed.  This is
 *     probably the biggest change required per connection.</li>
 *     <li><strong>readSize</strong> defines the size of the byte[] used for reading data into.
 *      Recent forks have modified this, so providing a method for customization seemed
 *      logical.</li>
 *     <li><strong>readTimeout</strong> defines the timeout between read attempts.  This was
 *      from the initial project, so it was left in there as a default to the original
 *      value.</li>
 * </ul>
 *
 * @author kendavidson
 */
abstract public class AbstractDeviceConnection implements DeviceConnection {

    /**
     * The socket to which this device is connected.
     */
    private final BluetoothSocket mSocket;

    /**
     * The InputStream from which the DeviceConnection is reading.
     */
    private InputStream mInStream;

    /**
     * OutputStream to which the DeviceConnetion writes.
     */
    private OutputStream mOutStream;

    /**
     * Status of the current connection.
     */
    private ConnectionStatus mConnectionStatus;

    /**
     * Connection properties
     */
    protected Properties mProperties;

    /**
     * Data is provided through this listener.  If there is no listener, the implementation is to
     * just build up data in the buffer until requested.  When a listener is added, the buffer
     * is read for all delimiters and all messages will be sent.
     */
    protected BiConsumer<BluetoothDevice, String> mOnDataReceived;

    /**
     * The connection has been cancelled and/or disconnected by the user.
     */
    protected BiConsumer<BluetoothDevice, Exception> mOnDisconnect;

    /**
     * Creates a new {@link AbstractDeviceConnection} to the provided NativeDevice, using the provided
     * Properties.
     *
     * @param socket
     * @param properties
     */
    public AbstractDeviceConnection(BluetoothSocket socket, Properties properties) throws IOException {
        this.mSocket = socket;
        this.mProperties = new Properties(properties);

        this.mConnectionStatus = ConnectionStatus.DISCONNECTED;

        this.mInStream = mSocket.getInputStream();
        this.mOutStream = mSocket.getOutputStream();
    }

    @Override
    public void run() {
        int bufferSize = StandardOption.READ_SIZE.get(mProperties);
        int readTimeout = StandardOption.READ_TIMEOUT.get(mProperties);

        mConnectionStatus = ConnectionStatus.CONNECTING;

        final byte[] buffer = new byte[bufferSize];
        int bytes;

        try {
            // The device will continue attempting to read until there is an IOException thrown
            // due to the other side disconnecting.  Apparently when the other side disconnects
            // mmStream.isConnected() still returns true.
            mConnectionStatus = ConnectionStatus.CONNECTED;
            while (mConnectionStatus == ConnectionStatus.CONNECTED) {
                bytes = mInStream.read(buffer);
                if (bytes > 0)
                    receivedData(Arrays.copyOf(buffer, bytes));

                if (readTimeout > 0)
                    Thread.sleep(readTimeout);
            }
        } catch (Exception e) {
            if (mConnectionStatus != ConnectionStatus.DISCONNECTING
                    && mOnDisconnect != null) {
                mOnDisconnect.accept(mSocket.getRemoteDevice(), e);
            }
        } finally {
            mConnectionStatus = ConnectionStatus.DISCONNECTED;

            // Finally clean up the streams, because we could have already done this during the
            // disconnect() it's possible they were already closed
            try { mInStream.close(); } catch (IOException ignored) { }
            try { mOutStream.close(); } catch (IOException ignored) { }
            try { mSocket.close(); } catch (IOException ignored) { }
        }
    }

    /**
     * Returns the BluetoothDevice to which this {@link DeviceConnection} is communicating.
     *
     * @return the BluetoothDevice for this connection
     */
    @Override
    public synchronized BluetoothDevice getDevice() {
        return mSocket.getRemoteDevice();
    }

    /**
     * Attempts to disconnect (gracefully) from the device.  This is done by setting the connection
     * status and closing the streams/socket.  Setting the status is the graceful part.
     *
     * @return whether the disconnect request was successful.
     */
    @Override
    public synchronized boolean disconnect() {
        mConnectionStatus = ConnectionStatus.DISCONNECTING;

        try { mInStream.close(); } catch (IOException ignored) { }
        try { mOutStream.close(); } catch (IOException ignored) { }
        try { mSocket.close(); } catch (IOException ignored) { }

        return true;
    }

    /**
     * Handle incoming data from the device.  The bytes provided are in raw form and can be modified
     * prior to saving, handling, etc.
     *
     * @param bytes the raw byte[] read from the device
     */
    protected abstract void receivedData(byte[] bytes);

    /**
     * Attempts to write data to the device.  If the bytes need to be encoded or modified prior
     * it's wise to override this method to do so.
     *
     * @param bytes correctly encoded byte[] to be written to device
     * @throws IOException if there was an error encoding bytes.
     */
    @Override
    public synchronized void write(byte[] bytes) throws IOException {
        mOutStream.write(bytes);
    }

    /**
     * Connection implementations should determine what constitutes available.  For example, a
     * delimited connection could return the number of individual methods, while a byte[]
     * connection would determine the total number of bytes.
     *
     * @return the number of data (messages/bytes) available for read(s)
     */
    public abstract int available();

    /**
     * Adds a listener used to communicate newly received data.  It's up to the implementation to
     * determine what should happen when there is a listener active.
     *
     * @param onDataReceived consumer of received data
     * @return whether
     */
    @Override
    public synchronized void onDataReceived(BiConsumer<BluetoothDevice,String> onDataReceived) {
        this.mOnDataReceived = onDataReceived;
    }

    /**
     * CLear the onDataReceived listener.
     *
     * @return
     */
    @Override
    public synchronized void clearOnDataReceived() {
        this.mOnDataReceived = null;
    }

    /**
     * Get the current connection status.
     *
     * @return
     */
    @Override
    public synchronized ConnectionStatus getConnectionStatus() {
        return mConnectionStatus;
    }

    /**
     * Add a disconnect listener.
     *
     * @param onDisconnect
     */
    @Override
    public synchronized void onDisconnect(BiConsumer<BluetoothDevice,Exception> onDisconnect) {
        this.mOnDisconnect = onDisconnect;
    }
}
