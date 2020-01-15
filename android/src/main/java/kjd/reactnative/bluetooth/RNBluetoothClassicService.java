package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import kjd.reactnative.CommonCharsets;

/**
 * Provides the communication threads and message delivering/handling for a single connected
 * Bluetooth Classic device.  More information on the Serial Port Profile (SPP) can be found at
 * https://en.wikipedia.org/wiki/List_of_Bluetooth_profiles#Serial_Port_Profile_(SPP).
 *
 * @author kenjdavidson
 *
 */
public class RNBluetoothClassicService {

    private static final String TAG = "BluetoothClassicService";
    private static final boolean D = BuildConfig.DEBUG;

    /**
     * Serial Port Protocol UUID
     */
    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * BluetoothAdapter
     */
    private BluetoothAdapter mAdapter;

    /**
     * Thread responsible for connecting to a new device
     */
    private ConnectThread mConnectThread;

    /**
     * Communication thread takes over once ConnectThread has successfully handed off.
     */
    private ConnectedThread mConnectedThread;

    /**
     * Allows for communication of incoming data
     */
    private BluetoothEventListener listener;

    /**
     * Current connection state
     */
    private DeviceState mState;

    /**
     * The Android {@link BluetoothDevice} to which this service is communicating.
     */
    private BluetoothDevice mDevice;

    /**
     * Constructor. Prepares a new RNBluetoothClassicService session.
     *
     * @param listener data received listener
     */
    RNBluetoothClassicService(BluetoothEventListener listener) {
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = DeviceState.DISCONNECTED;
        this.listener = listener;
        this.mDevice = null;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device {@link BluetoothDevice} to which we are connecting.
     */
    synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, String.format("Connecting to %s", device.getName()));

        if (DeviceState.CONNECTING.equals(mState)) {
            cancelConnectThread(); // Cancel any thread attempting to make a connection
        }

        cancelConnectedThread(); // Cancel any thread currently running a connection

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

        // Unsure about whether to set device while just connecting
        setState(DeviceState.CONNECTING, null);
    }

    /**
     * Check whether service is connected to device
     *
     * @return Is connected to device
     */
    boolean isConnected () {
        return DeviceState.CONNECTED.equals(getState());
    }

    /**
     * Return the currently connected device, null if none.
     *
     * @return connected device
     */
    BluetoothDevice connectedDevice() {
        return mDevice;
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    void write(byte[] out) {
        if (D) Log.d(TAG, "Write in service, state is " + DeviceState.CONNECTED.name());
        ConnectedThread r; // Create temporary object

        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (!isConnected()) return;
            r = mConnectedThread;
        }

        r.write(out); // Perform the write unsynchronized
    }

    /**
     * Stop all threads
     */
    synchronized void stop() {
        if (D) Log.d(TAG, "Stopping RNBluetoothClassic service");

        cancelConnectThread();
        cancelConnectedThread();

        setState(DeviceState.DISCONNECTED, null);
    }

    /**
     * Return the current connection state.
     */
    private synchronized DeviceState getState() {
        return mState;
    }

    /**
     * Set the current state of connection
     *
     * @param state the updated state
     */
    private synchronized void setState(DeviceState state, BluetoothDevice device) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mDevice = device;
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket associated to the connection device - creates ConnectedThread
     * @param device to which the connection was established
     */
    private synchronized void connectionSuccess(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, String.format("Connected to %s", device.getAddress()));

        cancelConnectThread(); // Cancel any thread attempting to make a connection
        cancelConnectedThread(); // Cancel any thread currently running a connection

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(device, socket);
        mConnectedThread.start();

        listener.onConnectionSuccess(device);
        setState(DeviceState.CONNECTED, device);
    }


    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     *
     * @param device to which the connection was attempted
     */
    private void connectionFailed(BluetoothDevice device, Throwable e) {
        listener.onConnectionFailed(device, e); // Send a failure message
        RNBluetoothClassicService.this.stop(); // Start the service over to restart listening mode
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(BluetoothDevice device, Throwable e) {
        listener.onConnectionLost(device, e);  // Send a failure message
        RNBluetoothClassicService.this.stop(); // Start the service over to restart listening mode
    }

    /**
     * Cancel connect thread
     */
    private void cancelConnectThread () {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    /**
     * Cancel connected thread
     */
    private void cancelConnectedThread () {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(DeviceState.DISCONNECTED, null);
    }

    /**
     * Attempts to connect to the requested BluetoothDevice.  Does so by:
     * <ul>
     *     <li>Attempts to create an RFCOMM socket to the device</li>
     *     <li>Attempts to connect to the created socket</li>
     *     <li>Initializes the notifies that the connection was successful allowing the service
     *      to continue.</li>
     * </ul>
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            setName("ConnectThread");

            this.mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (Exception e) {
                listener.onConnectionFailed(device, e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "BEGIN mConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                Log.d(TAG,String.format("Connecting to socket %s", mmDevice.getAddress()));

                mmSocket.connect();
            } catch (Exception connectException) {
                // Some 4.1 devices have problems, try an alternative way to connect
                // See https://github.com/don/RCTBluetoothSerialModule/issues/89
                try {
                    Log.i(TAG,"Trying fallback...");
                    mmSocket = (BluetoothSocket)
                            mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class})
                                    .invoke(mmDevice,1);
                    mmSocket.connect();
                } catch (Exception socketException) {
                    try {
                        mmSocket.close();
                    } catch (Exception e1) {
                        // Ignore and handle below
                    }

                    connectionFailed(mmDevice, socketException);
                    return;
                }
            } finally {
                synchronized (RNBluetoothClassicService.this) {
                    mConnectThread = null;
                }
            }

            Log.i(TAG,String.format("Connection to %s successful", mmDevice.getAddress()));
            connectionSuccess(mmSocket, mmDevice);  // Start the connected thread
        }

        /**
         * Cancels the thread by closing the socket.
         *
         * TODO update to isConnecting/isRunning to remove the forced socket close/exception
         */
        void cancel() {
            try {
                mmSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "close() of connect socket failed", e);
                listener.onError(e);
            }
        }
    }

    /**
     * ConnectedThread runs throughout a device connection/registration.  It's responsible for
     * sending and receiving data from the device and passing it along to the appropriate
     * handlers.
     * <p>
     * More information can be found
     * https://developer.android.com/guide/topics/connectivity/bluetooth#ManageAConnection
     * including a basic ConnectedThread example.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mmCancelled;

        ConnectedThread(BluetoothDevice device, BluetoothSocket socket) {
            if (D) Log.d(TAG, "Create ConnectedThread");
            mmDevice = device;
            mmSocket = socket;
            mmCancelled = false;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (Exception e) {
                Log.e(TAG, "temp sockets not created", e);
                listener.onError(e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected, the thread is currently cancelled
            // by closing the socket and causing the mmInStream.read() to fail, allowing the
            // Exception handling to notify.  This should be updated to use an isConnected
            // flag.
            while (!mmCancelled) {
                try {
                    if (mmInStream.available() > 0) {
                        bytes = mmInStream.read(buffer);
                        listener.onReceivedData(mmDevice, Arrays.copyOf(buffer, bytes));
                    }

                    Thread.sleep(500);      // Pause
                } catch (Exception e) {
                    Log.e(TAG, "Disconnected", e);

                    if (!mmCancelled) {
                        connectionLost(mmDevice, e);
                        RNBluetoothClassicService.this.stop(); // Start the service over to restart listening mode
                    }

                    break;
                }
            }

            try {
                // Finally attempt to close the socket
                mmSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer  The bytes to write
         */
        void write(byte[] buffer) {
            try {
                String str = new String(buffer, "UTF-8");
                if (D) Log.d(TAG, "Write in thread " + str);
                mmOutStream.write(str.getBytes());
            } catch (Exception e) {
                Log.e(TAG, "Exception during write", e);
                listener.onError(e);
            }
        }

        /**
         * Cancel the connection - gracefully
         */
        synchronized void cancel() {
            if (D) Log.d(TAG, String.format("Closing connection to %s", mmDevice.getAddress()));
            mmCancelled = true;
        }
    }
}
