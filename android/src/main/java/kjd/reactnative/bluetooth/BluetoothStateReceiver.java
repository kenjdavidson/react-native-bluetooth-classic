package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kjd.reactnative.RCTEventEmitter;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private RCTEventEmitter emitter;

    public BluetoothStateReceiver(RCTEventEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d(this.getClass().getSimpleName(), "Bluetooth was disabled");
                    emitter.sendEvent(BluetoothEvent.BLUETOOTH_DISABLED.code, null);
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(this.getClass().getSimpleName(), "Bluetooth was enabled");
                    emitter.sendEvent(BluetoothEvent.BLUETOOTH_ENABLED.code, null);
                    break;
            }
        }
    }
}
