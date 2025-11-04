package kjd.reactnative.bluetooth.event;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import kjd.reactnative.bluetooth.device.NativeDevice;

/**
 * Wraps device event data within an event containing the device and timestamp.
 * This is used for device connection/disconnection/discovery events.
 *
 * @author kendavidson
 */
public class BluetoothDeviceEvent extends BluetoothEvent {

    private NativeDevice device;

    public BluetoothDeviceEvent(EventType eventType, NativeDevice device) {
        super(eventType);
        this.device = device;
    }

    @Override
    public ReadableMap buildMap() {
        WritableMap map = Arguments.createMap();
        map.putMap("device", device.map());
        return map;
    }
}
