package kjd.reactnative.bluetooth.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import kjd.reactnative.bluetooth.RNBluetoothClassicModule;
import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Default ACL connection receiver, currently only listens on:
 * <ul>
 *  <li>{@link android.bluetooth.BluetoothDevice#ACTION_ACL_CONNECTED}</li>
 *  <li>{@link android.bluetooth.BluetoothDevice#ACTION_ACL_DISCONNECT_REQUESTED}</li>
 *  <li>{@link android.bluetooth.BluetoothDevice#ACTION_ACL_DISCONNECTED}</li>
 * </ul>
 * <p>
 * Can be built out to provide receiving other ACL events, although from initial testing they
 * didn't add much.  For more information on the available ACL (connection) events see
 * https://developer.android.com/reference/android/bluetooth/BluetoothDevice
 * <p>
 * This receiver is still mean to be used with from the {@link RNBluetoothClassicModule}
 * as it was super annoying to convert these all over.  Once I take a look at getting RxBluetooth
 * updated with more of the functionality that is already implemented, it'll get moved over.
 *
 * @author kendavidson
 *
 */
public class ActionACLReceiver extends BroadcastReceiver {

    private ActionACLCallback mCallback;

    public ActionACLReceiver(ActionACLCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            Log.d(this.getClass().getSimpleName(),
                    String.format("ACL Disconnect requested for device %s", device.getAddress()));
            mCallback.onACLDisconnectRequest(new NativeDevice(device));
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Log.d(this.getClass().getSimpleName(),
                    String.format("ACL Disconnected for device %s", device.getAddress()));
            mCallback.onACLDisconnected(new NativeDevice(device));
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            // This is currently ignored for now, all Connections should be managed by the device
            // telling the Module when it has successfully opened the connection.
            Log.d(this.getClass().getSimpleName(),
                    String.format("ACL Connected for device %s", device.getAddress()));
        } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            Log.d(this.getClass().getSimpleName(),
                    String.format("Connection state changed for device %s from %s to %s",
                            device.getAddress(),
                            intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1),
                            intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)));
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);

        return intent;
    }

    public interface ActionACLCallback {

        /**
         * When the {@link BluetoothAdapter} receives an ACTION_ACL_DISCONNECT_REQUESTED event.
         * This is the low level disconnect that's been used to actually remove connected
         * devices.  It doesn't work with Android to Android devices though, so other events
         * were added for logging.
         *
         * @param device
         */
        void onACLDisconnectRequest(NativeDevice device);

        /**
         * Unsure if this is required - it wasn't in the original library so it may be extra.
         *
         * @param device
         */
        void onACLDisconnected(NativeDevice device);

    }
}
