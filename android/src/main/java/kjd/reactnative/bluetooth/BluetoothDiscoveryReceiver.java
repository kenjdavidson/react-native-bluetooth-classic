package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import kjd.reactnative.RCTEventEmitter;

/**
 * Creates and updates device based on ACTION_FOUND intents.  Once completed the full list is
 * returned to the React Native client.
 *
 * TODO ACTION_FOUND should update React Native with an event, in order to live update the RSSI
 *
 * @author kendavidson
 *
 */
public class BluetoothDiscoveryReceiver extends BroadcastReceiver {

    private DiscoveryCompleteListener onComplete;
    private Map<String,NativeDevice> unpairedDevices;

    public BluetoothDiscoveryReceiver(DiscoveryCompleteListener listener) {
        this.onComplete = listener;
        this.unpairedDevices = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            NativeDevice nativeDevice = new NativeDevice(device);
            nativeDevice.addExtra("name", intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
            nativeDevice.addExtra("rssi", intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

            if (!unpairedDevices.containsKey(device.getAddress())) {
                if (BuildConfig.DEBUG)
                    Log.d(this.getClass().getSimpleName(), "onReceive found: " + nativeDevice);

                unpairedDevices.put(device.getAddress(), nativeDevice);
            } else {
                unpairedDevices.get(device.getAddress()).addExtra("rssi",
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            onComplete.onDiscoveryComplete(unpairedDevices.values());
        }
    }

    public interface DiscoveryCompleteListener {
        void onDiscoveryComplete(Collection<NativeDevice> unpairedDevices);
    }
}
