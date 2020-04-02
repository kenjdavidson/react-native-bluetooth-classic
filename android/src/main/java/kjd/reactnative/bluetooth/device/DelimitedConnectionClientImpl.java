package kjd.reactnative.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Properties;

import kjd.reactnative.bluetooth.BluetoothUUID;

/**
 * Client connection implementation of the a delimited connection.  The connection is implemented
 * using either insecure or secure RFCOMM socket.
 * <p>
 * The Android standard documentation provides the example on which this was based:
 * https://developer.android.com/guide/topics/connectivity/bluetooth#ManageAConnection
 *
 * @author kendavidson
 */
public class DelimitedConnectionClientImpl extends AbstractDelimitedConnection {

    private static final String TAG = DelimitedConnectionClientImpl.class.getSimpleName();

    private ConnectThread mConnectThread;

    @Override
    protected boolean startConnection(Properties properties) {
        setConnectionStatus(ConnectionStatus.CONNECTING);
        mConnectThread = new ConnectThread();
        mConnectThread.run();

        return true;
    }

    @Override
    protected boolean stopConnection() {
        mConnectThread.cancel();
        mConnectThread = null;
        return true;
    }

    /**
     * Performs a client connection to a paired BluetoothDevice.  If the initial attempt failed
     * a (reflective) work around is performed.
     *
     * @author kendavidson
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private boolean connected;

        ConnectThread() {
            BluetoothSocket tmp = null;

            try {
                tmp = getDevice().getDevice().createRfcommSocketToServiceRecord(BluetoothUUID.SPP.uuid);
            } catch (Exception e) {
                setConnectionStatus(ConnectionStatus.DISCONNECTED);
                getConnectionListener().onConnectionFailure(getDevice(), e);
            }

            mmSocket = tmp;
            connected = false;
        }

        public void run() {
            BluetoothDevice device = getDevice().getDevice();
            Log.d(TAG,"Attempting connection to " + getDevice().getAddress());

            try {
                mmSocket.connect();
            } catch (Exception connectException) {
                // Some 4.1 devices have problems, try an alternative way to connect
                // See https://github.com/don/RCTBluetoothSerialModule/issues/89
                try {
                    Log.i(TAG,"Connection failed, attempting fallback method");
                    mmSocket = (BluetoothSocket)
                            getDevice().getClass().getMethod("createRfcommSocket",
                                    new Class[] {int.class}).invoke(device,1);
                    mmSocket.connect();
                } catch (Exception socketException) {
                    try {
                        mmSocket.close();
                    } catch (Exception e1) {
                        // Ignore and handle below
                    }

                    setConnectionStatus(ConnectionStatus.DISCONNECTED);
                    getConnectionListener().onConnectionFailure(getDevice(), socketException);
                    return;
                }
            }

            onConnectionSuccess(mmSocket);
        }

        /**
         * Cancel should only be called while the thread is still live (unconnected).  Once the
         * connection has been established, the {@link BluetoothSocket} is passed to an implementation
         * of the ConnectedThread.
         * <p>
         * This means if we're already connected, just skip the cancel as there isn't anything to
         * do.
         */
        void cancel() {
            if (ConnectionStatus.CONNECTING == getConnectionStatus()) {
                setConnectionStatus(ConnectionStatus.DISCONNECTED);
                try { mmSocket.close(); } catch (IOException ignored) { }
            }
        }
    }
}
