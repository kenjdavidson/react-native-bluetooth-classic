package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kjd.reactnative.RCTEventEmitter;

public class BluetoothConnectionReceiver extends BroadcastReceiver {

    private RCTEventEmitter emitter;

    public BluetoothConnectionReceiver(RCTEventEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(this.getClass().getSimpleName(), "Device connected: " + device.toString());
            emitter.sendEvent(BluetoothEvent.BLUETOOTH_DISCONNECTED.code,
                    RNUtils.deviceToWritableMap(device));
        }
    }
}
