package kjd.reactnative.bluetooth.event;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public enum EventType {
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
     * <p>
     * I'm curious whether this is required after the change to Device based communication.  But
     * for now it will be left in.
     */
    DEVICE_CONNECTED("deviceConnected"),

    /**
     * Fired when a {@link android.bluetooth.BluetoothDevice} is disconnected.  Again this is not
     * the same thing as paired - but rather when a socket connection is lost.
     * <p>
     * I'm curious whether this is required after the change to Device based communication.  But
     * for now it will be left in.
     */
    DEVICE_DISCONNECTED("deviceDisconnected"),

    /**
     * Data is read from the device
     */
    DEVICE_READ("deviceRead"),

    /**
     * A general error occurs during processing.
     */
    ERROR("error"),

    /**
     * A number of requests/questions have come in asking if is possible to search for a specific
     * device.  This will give users the ability to listen for individual device(s) discovered
     * and manage that process.   For example, they could start discovery and then listen for
     * a specific device, once that device is found discovery can be stopped.
     */
    DEVICE_DISCOVERED("deviceDiscovered");

    public final String code;
    EventType(String code) {
        this.code = code;
    }

    public static WritableMap eventNames() {
        WritableMap events = Arguments.createMap();
        for(EventType event : EventType.values()) {
            events.putString(event.name(), event.name());
        }
        return events;
    }
}
