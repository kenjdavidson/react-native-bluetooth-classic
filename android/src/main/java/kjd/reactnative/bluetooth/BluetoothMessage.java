package kjd.reactnative.bluetooth;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.util.Calendar;
import java.util.Date;

/**
 * Wraps bluetooth message data within a message containing the device from which and timestamp
 * of when the message was received.  Provides customized data to be transferred by configuring
 * each connection with a specific type of DataTransformer (Byte[] to T).
 *
 * @param <T> type of data being transferred.
 */
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
        map.putMap("device", device);
        map.putString("data", String.valueOf(data));
        map.putString("timestamp", Utilities.formatDate(timestamp));
        return map;
    }
}
