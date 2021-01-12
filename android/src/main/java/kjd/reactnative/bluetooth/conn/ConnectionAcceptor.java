package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Responsible for managing how the {@link BluetoothAdapter} is placed into accept mode.
 *
 * @author kendavidson
 *
 */
public abstract class ConnectionAcceptor extends Thread {

    protected BluetoothAdapter mAdapter;
    protected Properties mProperties;

    private final Set<AcceptorListener<BluetoothSocket>> mListeners;

    protected ConnectionAcceptor(BluetoothAdapter adapter, Properties properties) throws IOException {
        this.mAdapter = adapter;
        this.mProperties = new Properties(properties);
        this.mListeners = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Provide an implementation to the accept logic and return the {@link BluetoothSocket} which
     * was accepted.
     *
     * @param properties the connection {@link Properties} from the app
     * @return the {@link BluetoothSocket} of the app connected.
     * @throws IOException if an error occurs during connection
     */
    protected abstract BluetoothSocket connect(Properties properties) throws IOException;

    /**
     * Provide appropriate cancel logic.  This will most likely force close the server socket
     * and then handle that change internally.
     */
    public abstract void cancel();

    @Override
    final public void run() {
        try {
            BluetoothSocket results = connect(mProperties);
            notifyListeners(results);
        } catch (Exception e) {
            notifyListeners(e);
        }
    }

    protected void notifyListeners(BluetoothSocket result) {
        for (AcceptorListener<BluetoothSocket> listener : mListeners) {
            listener.success(result);
        }
    }

    protected void notifyListeners(Exception e) {
        for (AcceptorListener<BluetoothSocket> listener : mListeners) {
            listener.failure(e);
        }
    }

    public void addListener(AcceptorListener<BluetoothSocket> listener) {
        mListeners.add(listener);
    }

    /**
     * Allow communication between the {@link ConnectionAcceptor} and it's caller.  This will
     * need to be updated, at some point, to allow for multiple connections to be established, but
     * at this point with no specific use cases I'm not sure the best way to do it.
     * <ul>
     *     <li>Make multiple calls to {@link #success(BluetoothSocket)} and then return the last
     *     device connected when it's finished?</li>
     *     <li>Add another success method that accepts a {@link BluetoothSocket}[] at the end</li>
     * </ul>
     *
     * @param <BluetoothSocket>
     */
    public interface AcceptorListener<BluetoothSocket> {
        void success(BluetoothSocket result);
        void failure(Exception e);
    }

}
