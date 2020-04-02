package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothAdapter;

public enum BluetoothState {
    DISABLED(BluetoothAdapter.STATE_OFF),
    ENABLED(BluetoothAdapter.STATE_ON);

    public final int code;
    private BluetoothState(int code) {
        this.code = code;
    }
}
