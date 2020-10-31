package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class Utilities {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static String parseDate(Date date) {
        return sdf.format(date);
    }

    public static Properties mapToProperties(ReadableMap readableMap) {
        Map<String,Object> map = readableMap.toHashMap();
        Properties properties = new Properties();
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
    }
}

