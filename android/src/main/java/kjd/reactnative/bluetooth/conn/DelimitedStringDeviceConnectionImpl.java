package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Implements a {@link DeviceConnection} which manages the received data within a
 * buffer.  Messages are {@link #read()} in blocks based on the delimiter provided, this is also the case
 * for {@link #available()}.
 *
 * @author kendavidson
 *
 */
public class DelimitedStringDeviceConnectionImpl extends AbstractDeviceConnection {

    /**
     * The buffer in which data is stored.
     */
    private final StringBuffer mBuffer;

    /**
     * The delimiter - easy access from properties.
     */
    private final String mDelimiter;

    /**
     * The charset used to decode the inbound data.
     */
    private final Charset mCharset;

    /**
     * Creates a new {@link AbstractDeviceConnection} to the provided NativeDevice, using the provided
     * Properties.
     *
     * @param socket
     * @param properties
     */
    public DelimitedStringDeviceConnectionImpl(BluetoothSocket socket, Properties properties) throws IOException {
        super(socket, properties);

        this.mBuffer = new StringBuffer();
        this.mDelimiter = StandardOption.DELIMITER.get(properties);
        this.mCharset = StandardOption.DEVICE_CHARSET.get(properties);
    }

    @Override
    protected void receivedData(byte[] bytes) {
        Log.d(this.getClass().getSimpleName(),
                String.format("Received %d bytes from device %s", bytes.length, getDevice().getAddress()));

        mBuffer.append(new String(bytes, mCharset));

        if (mOnDataReceived != null) {
            Log.d(this.getClass().getSimpleName(),
                    "BluetoothEvent.READ listener is registered, providing data");

            String message;
            while ((message = read()) != null) {
                mOnDataReceived.accept(getDevice(), message);
            }
        } else {
            Log.d(this.getClass().getSimpleName(),
                    "No BluetoothEvent.READ listeners are registered, skipping handling of the event");
        }
    }

    /**
     * Provides the number of full messages (delimiters) available within the buffer.
     *
     * @return the number of messages available
     */
    @Override
    public int available() {
        int count = 0;
        int lastIndex = -1;

        while ((lastIndex = mBuffer.indexOf(mDelimiter, lastIndex+1)) > -1) {
            count++;
        }

        return count;
    }

    @Override
    public synchronized boolean clear() {
        mBuffer.delete(0, mBuffer.length());
        return true;
    }

    /**
     * Reads the next message in from the buffer, based on the configured delimiter.
     *
     * @return the next message from the buffer
     * @throws IOException if an error occurs during reading
     */
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

}
