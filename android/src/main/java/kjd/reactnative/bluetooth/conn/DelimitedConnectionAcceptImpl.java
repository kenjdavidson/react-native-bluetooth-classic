package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import kjd.reactnative.bluetooth.BluetoothUUID;
import kjd.reactnative.bluetooth.device.NativeDevice;

public class DelimitedConnectionAcceptImpl extends AbstractDelimitedConnection {

    private static final String TAG = DelimitedConnectionAcceptImpl.class.getSimpleName();

    private AcceptThread mAcceptThread;
    private String mServiceName;
    private boolean mSecure;

    @Override
    protected boolean startConnection(Properties properties) {
        this.mSecure = properties.contains(StandardOptions.Secure.code())
                ? (Boolean) properties.get(StandardOptions.Secure.code())
                : StandardOptions.Secure.defaultValue();
        this.mServiceName = properties.contains(StandardOptions.ServiceName)
                ? properties.getProperty(StandardOptions.ServiceName.code())
                : StandardOptions.ServiceName.defaultValue();


        setConnectionStatus(ConnectionStatus.CONNECTING);
        this. mAcceptThread = new AcceptThread();
        this.mAcceptThread.start();
        return true;
    }

    @Override
    protected boolean stopConnection() {
        mAcceptThread.cancel();
        mAcceptThread = null;
        return true;
    }

    /**
     * A cancellable thread used for waiting for client connections.  There is currently no timeout,
     * but the request can be cancelled (albeit not pretty) it seems to be working.
     *
     * @author tpettrov
     */
    private class AcceptThread extends Thread {
        private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        private final BluetoothServerSocket mmServerSocket;
        private AtomicBoolean cancelled = new AtomicBoolean(false);


        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                if (mSecure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(mServiceName,
                            BluetoothUUID.SPP.uuid);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(mServiceName,
                            BluetoothUUID.SPP.uuid);
                }
            } catch (IOException e) {
                setConnectionStatus(ConnectionStatus.DISCONNECTED);
                getConnectionListener().onError(getDevice(), e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;

            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    if (cancelled.get()) {
                        Log.d(TAG, "Socket accept() was cancelled");
                        return;
                    }

                    setConnectionStatus(ConnectionStatus.DISCONNECTED);
                    getConnectionListener().onError(getDevice(), e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    setDevice(new NativeDevice(socket.getRemoteDevice()));
                    onConnectionSuccess(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        setConnectionStatus(ConnectionStatus.DISCONNECTED);
                        getConnectionListener().onError(getDevice(), e);
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            if (ConnectionStatus.CONNECTING == getConnectionStatus()) {
                setConnectionStatus(ConnectionStatus.DISCONNECTED);
                try { mmServerSocket.close(); } catch (IOException ignored) { }
            }
        }
    }

}
