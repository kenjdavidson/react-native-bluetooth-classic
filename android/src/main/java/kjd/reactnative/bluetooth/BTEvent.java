package kjd.reactnative.bluetooth;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum BTEvent {
    BLUETOOTH_ENABLED("bluetoothEnabled"),
    BLUETOOTH_DISABLED("bluetoothDisabled"),
    BLUETOOTH_CONNECTED("bluetoothConnected"),
    BLUETOOTH_DISCONNECTED("bluetoothDisconnected"),
    CONNECTION_SUCCESS("connectionSuccess"),        // Promise only
    CONNECTION_FAILED("connectionFailed"),          // Promise only
    CONNECTION_LOST("connectionLost"),
    READ("read"),
    ERROR("error");

    public final String code;
    private BTEvent(String code) {
        this.code = code;
    }

    public static WritableMap eventNames() {
        WritableMap events = Arguments.createMap();
        Arrays.stream(BTEvent.values()).forEach(e -> events.putString(e.name(), e.code));
        return events;
    }
}
