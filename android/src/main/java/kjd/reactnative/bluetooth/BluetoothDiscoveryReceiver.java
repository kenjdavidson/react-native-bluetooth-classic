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

public class BluetoothDiscoveryReceiver extends BroadcastReceiver {

    private DiscoveryCompleteListener onComplete;
    private Map<String,BluetoothDevice> unpairedDevices;

    public BluetoothDiscoveryReceiver(DiscoveryCompleteListener listener) {
        this.onComplete = listener;
        this.unpairedDevices = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(this.getClass().getSimpleName(), "Bluetooth Discovery Receiver: " + action);

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (!unpairedDevices.containsKey(device.getAddress())) {
                unpairedDevices.put(device.getAddress(), device);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            onComplete.onDiscoveryComplete(unpairedDevices.values());
        }
    }

    public interface DiscoveryCompleteListener {
        void onDiscoveryComplete(Collection<BluetoothDevice> unpairedDevices);
    }
}
