package kjd.reactnative.bluetooth.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;

import kjd.reactnative.bluetooth.BuildConfig;
import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Handles receiving pairing and un-pairing requests through the provided {@link PairingCallback}.
 * {@link PairingReceiver}(s) are self un-registering, by implementing {@link LifecycleEventListener}
 * and applying the un-register to {@link LifecycleEventListener#onHostPause()} and
 * {@link LifecycleEventListener#onHostDestroy()}.
 *
 * @author kdavidson
 *
 */
public class PairingReceiver extends BroadcastReceiver
        implements LifecycleEventListener {

    private static final String TAG = PairingReceiver.class.getSimpleName();

    private ReactApplicationContext mContext;
    private PairingCallback mCallback;

    public PairingReceiver(ReactApplicationContext context, PairingCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
            final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final NativeDevice wrapper = new NativeDevice(device);

            if (state == BluetoothDevice.BOND_BONDED) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("Successfully paired with device %s", device.getAddress()));

                mCallback.onPairingSuccess(wrapper);
                context.unregisterReceiver(this);
            } else if (state == BluetoothDevice.BOND_NONE){
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("Completed un-pairing with device %s", device.getAddress()));

                mCallback.onPairingSuccess(wrapper);
                context.unregisterReceiver(this);
            }
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        return intentFilter;
    }

    @Override
    public void onHostResume() {
        // Ignored since we have no way to get back
    }

    @Override
    public void onHostPause() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onHostDestroy() {
        mContext.unregisterReceiver(this);
    }

    public interface PairingCallback {

        /**
         * Whether a successful pairing/bonding or unpairing/unbonding was completed.
         *
         * @param device
         */
        void onPairingSuccess(NativeDevice device);

        /**
         * If an exception occurs while attempting to bond/pair or unbond/pair.
         *
         * @param cause
         */
        void onPairingFailure(Exception cause);

    }
}
