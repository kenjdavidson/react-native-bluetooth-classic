package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;

import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Implements the comment features for a delimited device connection.  Delimited
 * connections are generally String based which parse / concatenate received messages
 * by their specified delimiter.  The standard properties required for this
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
abstract public class AbstractDelimitedConnection
        implements DeviceConnection {

    public static final String PROP_DELIMITER = "delimiter";
    public static final String PROP_READ_TIMEOUT = "readTimeout";
    public static final String PROP_READ_SIZE = "readSize";
    public static final String PROP_CHARSET = "charset";

    public static final String DEFAULT_DELIMITER = "\n";
    public static final int DEFAULT_READ_TIMEOUT = 300;
    public static final int DEFAULT_READ_SIZE = 1024;
    public static final Charset DEFAULT_CHARSET = Charset.forName("ascii");

    /**
     * The {@link NativeDevice} wrapper.
     */
    private NativeDevice mDevice;

    /**
     * {@link Charset} used to encode/decode data to/from the device.  There is currently no checking
     * here, so be careful as you'll get an exception if not available.
     */
    private Charset mCharset;

    /**
     * Delimiter used to split data.  Setting a delimiter of null/blank will not split data
     * and cause all the message to be sent.
     */
    private String mDelimiter;

    /**
     * Provides a method for adding time between read attempts.  This was a hold over from the
     * original library that was converted to a config.
     */
    private int mReadTimeout;

    /**
     * Size of the buffer available for reading.
     */
    private int mReadSize;

    /**
     * Connection related details emitted through this listener.
     */
    private DeviceConnectionListener mConnListener;

    /**
     * Data is provided through this listener.  If there is no listener, the implementation is to
     * just build up data in the buffer until requested.  When a listener is added, the buffer
     * is read for all delimiters and all messages will be sent.
     */
    private DataReceivedListener mDataListener;

    /**
     * Maintains all the data received from the device.  This will be read delimter by delimiter
     * when requested.
     */
    private StringBuffer mBuffer;

    /**
     * The thread managing the connection.
     */
    private ConnectedThread mConnectedThread;

    /**
     * Status of the current connection.
     */
    private ConnectionStatus mConnectionStatus;

    @Override
    public synchronized NativeDevice getDevice() {
        return mDevice;
    }

    protected synchronized void setDevice(NativeDevice device) {
        this.mDevice = device;
    }

    @Override
    public synchronized boolean connect(NativeDevice device,
                                        Properties properties,
                                        DeviceConnectionListener listener) {
        this.mDevice = device;
        this.mConnListener = listener;
        this.mBuffer = new StringBuffer();
        this.mConnectionStatus = ConnectionStatus.DISCONNECTED;

        this.mCharset = properties.containsKey(PROP_CHARSET)
                ? Charset.forName(properties.getProperty(PROP_CHARSET)) : DEFAULT_CHARSET;
        this.mDelimiter = properties.containsKey(PROP_DELIMITER)
                ? properties.getProperty(PROP_DELIMITER) : DEFAULT_DELIMITER;
        this.mReadSize = properties.containsKey(PROP_READ_SIZE)
                ? (Integer) properties.get(PROP_READ_SIZE) : DEFAULT_READ_SIZE;
        this.mReadTimeout = properties.containsKey(PROP_READ_TIMEOUT)
                ? (Integer) properties.get(PROP_READ_TIMEOUT) : DEFAULT_READ_TIMEOUT;

        return startConnection(properties);
    }

    @Override
    public boolean disconnect() {
        if (ConnectionStatus.CONNECTING == mConnectionStatus) {
            stopConnection();
        }

        if (mConnectedThread != null) { // Clear the connected thread if it exists
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        return true;
    }

    /**
     * Implementation can perform any connection required.
     *
     * @param properties {@link Properties} required for the connection.
     * @return returns whether the Connection was started
     */
    abstract protected boolean startConnection(Properties properties);

    /**
     * Stops the connection.
     *
     * @return whether the connection was stopped
     */
    abstract protected boolean stopConnection();

    /**
     * Called when a connection is completed.  This should probably be updated to pass onSuccess
     * and onFailure handlers to the start and stop requests, but for now this is less refactoring
     * and seems to work.
     *
     * @param socket the {@link BluetoothSocket} to the current device
     */
    synchronized void onConnectionSuccess(BluetoothSocket socket) {
        mConnectionStatus = ConnectionStatus.CONNECTED;
        stopConnection();

        // Let the module know we have a successful connection and then continue on to start
        // the reading process
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    protected synchronized void onReceivedData(byte[] bytes) {
        Log.d(this.getClass().getSimpleName(),
                String.format("Received %d bytes from device %s", bytes.length, mDevice.getAddress()));

        mBuffer.append(decodeData(bytes));

        if (mDataListener != null) {
            Log.d(this.getClass().getSimpleName(),
                    "DEVICE_READ listener is registered, providing data");

            String message;
            while ((message = read()) != null) {
                mDataListener.onDataReceived(mDevice, message);
            }
        } else {
            Log.d(this.getClass().getSimpleName(),
                    "No BTEvent.READ listeners are registered, skipping handling of the event");
        }
    }

    /**
     * Provides a method for decoding data from the device.  This method is passed through before
     * the data is written to the buffer as a string.  This implementation decodes the bytes
     * using the provided {@link Charset}.
     *
     * @param bytes
     * @return
     */
    protected String decodeData(byte[] bytes) {
        return new String(bytes, mCharset);
    }

    @Override
    public String read() {
        String message = null;
        int index = mBuffer.indexOf(mDelimiter, 0);
        if (index > -1) {
            int len = index + mDelimiter.length();
            message = mBuffer.substring(0, len);
            mBuffer.delete(0, len);
        }
        return message;
    }

    @Override
    public synchronized boolean clear() {
        mBuffer.setLength(0);
        return true;
    }

    @Override
    public synchronized int available() {
        int count = 0;
        int lastIndex = -1;

        while ((lastIndex = mBuffer.indexOf(mDelimiter, lastIndex+1)) > -1) {
            count++;
        }

        return count;
    }

    @Override
    public synchronized void write(byte[] data) throws IOException {
        assertConnection();

        mConnectedThread.write(encodeData(data));
    }

    /**
     * Provides a method for overriding the encoding of data prior to writing to the device.  This
     * implementation assumes that the byte[] has already been encoded correctly from the client
     * and passes it along to the device.
     *
     * @param data the data which may be further encoded
     * @return encoded data
     */
    protected byte[] encodeData(byte[] data) {
        return data;
    }

    @Override
    public synchronized boolean addDeviceListener(DataReceivedListener listener) {
        this.mDataListener = listener;
        return true;
    }

    @Override
    public synchronized boolean removeDeviceListener() {
        this.mDataListener = null;
        return true;
    }

    @Override
    public synchronized ConnectionStatus getConnectionStatus() {
        return mConnectionStatus;
    }

    protected synchronized void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.mConnectionStatus = connectionStatus;
    }

    protected DeviceConnectionListener getConnectionListener() {
        return mConnListener;
    }

    private void assertConnection() {
        if (mDevice == null) {
            throw new IllegalStateException("There is no currently connected device");
        }
        if (mConnectedThread == null) {
            throw new IllegalStateException("Device does not currently have an open connection");
        }
    }

    /**
     * Thread handling the communication with the actual device.  Reading and writing
     * to this is managed through the DeviceConnection.
     * <p>
     * More information can be found
     * https://developer.android.com/guide/topics/connectivity/bluetooth#ManageAConnection
     * including a basic ConnectedThread example.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;
        private boolean mCancelled;

        ConnectedThread(BluetoothSocket socket) {
            this.mSocket = socket;
            this.mCancelled = false;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (Exception e) {
                mConnListener.onConnectionFailure(mDevice, e);
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {
            mConnListener.onConnectionSuccess(getDevice());
            Log.d(AbstractDelimitedConnection.this.getClass().getSimpleName(),
                    "Starting ConnectedThread");
            final byte[] buffer = new byte[mReadSize];
            int bytes;

            try {
                // The device will continue attempting to read until there is an IOException thrown
                // due to the other side disconnecting.  Apparently when the other side disconnects
                // mmStream.isConnected() still returns true.
                while (!mCancelled) {
                    bytes = mInStream.read(buffer);
                    if (bytes > 0)
                        onReceivedData(Arrays.copyOf(buffer, bytes));

                    if (mReadTimeout > 0)
                        Thread.sleep(mReadTimeout);
                }
            } catch (Exception e) {
                if (!mCancelled) {
                    Log.e(this.getClass().getSimpleName(),
                            "Disconnected but not cancelled", e);

                    mConnListener.onConnectionLost(mDevice, e);
                }
            } finally {
                try { mInStream.close(); } catch (IOException e) { }
                try { mOutStream.close(); } catch (IOException e) { }
                try { mSocket.close(); } catch (IOException e) { }
            }
        }

        /**
         * Writes the specified bytes to the {@link ConnectedThread}.
         *
         * @param bytes
         */
        synchronized void write(byte[] bytes) {
            try {
                mOutStream.write(bytes);
            } catch (IOException e) {
                mConnListener.onError(mDevice, e);
            }
        }

        synchronized void cancel() {
            mCancelled = true;

            // Force the connection closed
            // This is required since the READ method has no timeout so it will just block
            try { mInStream.close(); } catch (IOException e) { }
            try { mOutStream.close(); } catch (IOException e) { }
            try { mSocket.close(); } catch (IOException e) { }
        }
    }
}
