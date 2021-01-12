package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Responsible for performing the actual connection to a {@link BluetoothDevice}.  Implementations
 * will accept the {@link BluetoothDevice} and connection {@link Properties} and attempt
 * to establish a {@link BluetoothSocket}.
 *
 * @author kendavidson
 */
public abstract class ConnectionConnector extends Thread {

    protected BluetoothDevice device;
    protected Properties properties;

    private final Set<ConnectorListener<BluetoothSocket>> listeners;

    public ConnectionConnector(BluetoothDevice device, Properties properties) throws IOException {
        this.device = device;
        this.properties = new Properties(properties);
        this.listeners = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Establish and return a {@link BluetoothSocket}.
     *
     * @param properties the {@link Properties} provided by the app
     * @return the {@link BluetoothSocket} which will be provided to the module
     * @throws IOException if an error occurs during read
     */
    protected abstract BluetoothSocket connect(Properties properties) throws IOException;

    /**
     * Provide appropriate cancel logic.  In most cases connectors are quickly successful or
     * not, so cancel most likely won't get called.
     */
    protected abstract void cancel();

    @Override
    final public void run() {
        try {
            BluetoothSocket result = connect(properties);
            notifyListeners(result);
        } catch (Exception e) {
            notifyListeners(e);
        }
    }

    private void notifyListeners(BluetoothSocket result) {
        for (ConnectorListener<BluetoothSocket> listener : listeners) {
            listener.success(result);
        }
    }

    private void notifyListeners(Exception e) {
        for (ConnectorListener<BluetoothSocket> listener : listeners) {
            listener.failure(e);
        }
    }

    public void addListener(ConnectorListener<BluetoothSocket> listener) {
        listeners.add(listener);
    }

    /**
     * Responsible for providing the {@link ConnectionConnector} a method for communication with
     * it's caller.  As the calls are done from the {@link ConnectionConnector} thread it's
     * important that the methods are synchronized appropriately.
     *
     * @param <BluetoothSocket>
     */
    public interface ConnectorListener<BluetoothSocket> {
        void success(BluetoothSocket socket);
        void failure(Exception e);
    }
}
