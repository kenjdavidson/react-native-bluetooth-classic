package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RNUtils {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static String parseDate(Date date) {
        return sdf.format(date);
    }

    public static String parseCalendar(Calendar cal) {
        return sdf.format(cal.getTime());
    }

}

