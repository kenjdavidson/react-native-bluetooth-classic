package kjd.reactnative.bluetooth;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.util.Calendar;
import java.util.Date;

public class BluetoothMessage<T> {

    private WritableMap device;
    private Date timestamp;
    private T data;

    public BluetoothMessage(WritableMap device, T data) {
        this.device = device;
        this.data = data;
        this.timestamp = Calendar.getInstance().getTime();
    }

    public WritableMap asMap() {
        WritableMap map = Arguments.createMap();
        map.putString("data", String.valueOf(data));
        map.putString("timestamp", RNUtils.parseDate(timestamp));
        return map;
    }
}
