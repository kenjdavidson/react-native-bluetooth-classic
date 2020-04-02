package kjd.reactnative.bluetooth.event;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public enum BluetoothEvent {
    /**
     * Fired when the {@link android.bluetooth.BluetoothAdapter} is enabled.
     */
    BLUETOOTH_ENABLED("bluetoothEnabled"),

    /**
     * Fired when the {@link android.bluetooth.BluetoothAdapter} is disabled.
     */
    BLUETOOTH_DISABLED("bluetoothDisabled"),

    /**
     * Fired when a {@link android.bluetooth.BluetoothDevice} is connected.  This is not the same
     * thing as paired - it's an actual socket connection being established.  Previously this
     * was the primary method for knowing when the device connection was successful, it's best
     * that the connection {@link com.facebook.react.bridge.Promise} be used instead.
     */
    DEVICE_CONNECTED("deviceConnected"),
    DEVICE_DISCONNECTED("deviceDisconnected"),
    DEVICE_READ("deviceRead"),
    ERROR("error");

    public final String code;
    BluetoothEvent(String code) {
        this.code = code;
    }

    public static WritableMap eventNames() {
        WritableMap events = Arguments.createMap();
        for(BluetoothEvent event : BluetoothEvent.values()) {
            events.putString(event.name(), event.name());
        }
        return events;
    }
}
