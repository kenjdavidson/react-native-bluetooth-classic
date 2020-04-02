package kjd.reactnative.bluetooth.event;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import kjd.reactnative.Mappable;
import kjd.reactnative.bluetooth.BluetoothState;

public class BluetoothStateChangeEvent implements Mappable {

    private BluetoothState state;

    public BluetoothStateChangeEvent(BluetoothState state) {
        this.state = state;
    }

    @Override
    public WritableMap map() {
        WritableMap map = Arguments.createMap();
        map.putString("state", state.name());
        map.putBoolean("enabled", BluetoothState.ENABLED == state);
        return map;
    }
}
