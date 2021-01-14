package kjd.reactnative.bluetooth.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Responsible for receiving and managing the {@link BluetoothDevice#ACTION_FOUND} and
 * {@link BluetoothAdapter#ACTION_DISCOVERY_FINISHED}.  The {@link DiscoveryReceiver} is
 * self un-registering, when it receives an ACTION_DISCOVERY_FINISHED action it will remove
 * itself, this applies to being cancelled as well.
 *
 * @author kendavidson
 *
 * TODO Build out with other DISCOVERY related actions (STARTED and REQUEST)
 */
public class DiscoveryReceiver extends BroadcastReceiver {

    private DiscoveryCallback mCallback;
    private Map<String, NativeDevice> unpairedDevices;

    public DiscoveryReceiver(DiscoveryCallback callback) {
        this.mCallback = callback;
        this.unpairedDevices = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Log.d(this.getClass().getSimpleName(),
                    String.format("Discovery found device %s", device.getAddress()));

            // Devices can be found multiple times, we don't want to duplicate the process if
            // we've found one.  Although, we can update the RSSI value I guess.  But that's for
            // another time.
            if (!unpairedDevices.containsKey(device.getAddress())) {
                NativeDevice found = new NativeDevice(device);
                found.putExtra("rssi", intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

                mCallback.onDeviceDiscovered(found);
                unpairedDevices.put(device.getAddress(), found);

                mCallback.onDeviceDiscovered(found);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.d(this.getClass().getSimpleName(),
                    String.format("Discovery found %d device(s)", unpairedDevices.size()));

            mCallback.onDiscoveryFinished(unpairedDevices.values());
            context.unregisterReceiver(this);
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        return intentFilter;
    }

    public interface DiscoveryCallback {

        /**
         * Alerts when {@link android.bluetooth.BluetoothDevice#ACTION_FOUND} is fired.  During discovery
         * devices will be found multiple times; differing values (such as RSSI) will be updated.
         *
         * @param device
         */
        void onDeviceDiscovered(NativeDevice device);

        /**
         * When discovery is completed a {@link android.bluetooth.BluetoothAdapter#ACTION_DISCOVERY_FINISHED}
         * a {@link List} of {@link NativeDevice}(s) is returned.
         *
         * @param devices
         */
        void onDiscoveryFinished(Collection<NativeDevice> devices);

        /**
         * If an {@link Exception} of any kind is thrown during the discovery process.
         *
         * @param e
         */
        void onDiscoveryFailed(Throwable e);

    }

}
