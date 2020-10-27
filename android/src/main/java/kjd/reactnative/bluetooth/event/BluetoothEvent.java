package kjd.reactnative.bluetooth.event;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.time.LocalDateTime;
import java.util.Calendar;

import kjd.reactnative.Mappable;
import kjd.reactnative.bluetooth.device.NativeDevice;

public abstract class BluetoothEvent implements Mappable {

    private EventType eventType;

    public BluetoothEvent(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public WritableMap map() {
        WritableMap map = Arguments.createMap();
        map.putString("eventType", eventType.name());
        map.putString("timestamp", LocalDateTime.now().toString());
        map.merge(buildMap());
        return map;
    }

    /**
     * Applies custom information for the event.
     *
     * @return
     */
    public abstract ReadableMap buildMap();

}
