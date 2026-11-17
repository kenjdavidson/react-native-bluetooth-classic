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
 * <p>
 * A receiver waits for one device and one outcome: {@link BluetoothDevice#BOND_BONDED} when
 * pairing, {@link BluetoothDevice#BOND_NONE} when un-pairing. Bond changes of other devices
 * are ignored, and a pairing that ends in {@link BluetoothDevice#BOND_NONE} is a failure.
 *
 * @author kdavidson
 *
 */
public class PairingReceiver extends BroadcastReceiver
        implements LifecycleEventListener {

    private static final String TAG = PairingReceiver.class.getSimpleName();

    private ReactApplicationContext mContext;
    private BluetoothDevice mDevice;
    private boolean mPairing;
    private PairingCallback mCallback;

    /**
     * @param context
     * @param device the device being paired or un-paired
     * @param pairing true when waiting for a pairing, false when waiting for an un-pairing
     * @param callback
     */
    public PairingReceiver(ReactApplicationContext context, BluetoothDevice device,
                           boolean pairing, PairingCallback callback) {
        this.mContext = context;
        this.mDevice = device;
        this.mPairing = pairing;
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction()))
            return;

        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device == null || !device.equals(mDevice))
            return;

        final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
        final NativeDevice wrapper = new NativeDevice(device);

        if (state == BluetoothDevice.BOND_BONDED && mPairing) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("Successfully paired with device %s", device.getAddress()));

            mCallback.onPairingSuccess(wrapper);
        } else if (state == BluetoothDevice.BOND_NONE && !mPairing) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("Completed un-pairing with device %s", device.getAddress()));

            mCallback.onPairingSuccess(wrapper);
        } else if (state == BluetoothDevice.BOND_NONE) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("Pairing with device %s failed", device.getAddress()));

            mCallback.onPairingFailure(wrapper);
        } else {
            // BOND_BONDING, keep waiting
            return;
        }

        context.unregisterReceiver(this);
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
         * The device reached the requested bond state: bonded when pairing, not bonded when
         * un-pairing.
         *
         * @param device
         */
        void onPairingSuccess(NativeDevice device);

        /**
         * Pairing ended without a bond: the request was rejected, cancelled or timed out.
         *
         * @param device
         */
        void onPairingFailure(NativeDevice device);

    }
}
