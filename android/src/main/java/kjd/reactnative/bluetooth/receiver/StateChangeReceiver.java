package kjd.reactnative.bluetooth.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import kjd.reactnative.bluetooth.RNBluetoothClassicModule;
import kjd.reactnative.bluetooth.BluetoothState;
import kjd.reactnative.bluetooth.BuildConfig;

/**
 * Provides listening to {@link BluetoothAdapter#ACTION_STATE_CHANGED} events.
 * <p>
 * This receiver is still mean to be used with from the {@link RNBluetoothClassicModule}
 * as it was super annoying to convert these all over.  Once I take a look at getting RxBluetooth
 * updated with more of the functionality that is already implemented, it'll get moved over.
 *
 * @author kendavidson
 *
 */
public class StateChangeReceiver extends BroadcastReceiver {

    private StateChangeCallback mCallback;

    public StateChangeReceiver(StateChangeCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    if (BuildConfig.DEBUG)
                        Log.d(this.getClass().getSimpleName(), "Bluetooth is now disabled");

                    mCallback.onStateChange(BluetoothState.DISABLED, BluetoothState.ENABLED);
                    break;
                case BluetoothAdapter.STATE_ON:
                    if (BuildConfig.DEBUG)
                        Log.d(this.getClass().getSimpleName(), "Bluetooth is now enabled");

                    mCallback.onStateChange(BluetoothState.ENABLED, BluetoothState.DISABLED);
                    break;
            }
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        return intent;
    }

    public interface StateChangeCallback {

        void onStateChange(BluetoothState newState, BluetoothState oldState);

    }
}

