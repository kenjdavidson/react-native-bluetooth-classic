package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link BluetoothDevice} wrapper supporting extras available from Intents during specific
 * actions.  For example, RSSI becomes available during discovery.
 *
 * @author kendavidson
 *
 */
public class NativeDevice implements MapWritable {

    private BluetoothDevice device;
    private Map<String,Object> extra;

    public NativeDevice(BluetoothDevice device) {
        this.device = device;
        this.extra = new HashMap<String,Object>();
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void addExtra(String name, Object value) {
        extra.put(name, value);
    }

    public <T> T getExtra(String name) {
        return (T) extra.get(name);
    }

    public WritableMap map() {
        if (device == null)
            return null;

        WritableMap map = Arguments.createMap();

        map.putString("name", device.getName());
        map.putString("address", device.getAddress());
        map.putString("id", device.getAddress());
        map.putInt("class", (device.getBluetoothClass() != null)
                ? device.getBluetoothClass().getDeviceClass() : -1);

        map.putMap("extra", Arguments.makeNativeMap(extra));

        WritableMap map1 = Arguments.makeNativeMap(extra);

        return map;
    }
}
