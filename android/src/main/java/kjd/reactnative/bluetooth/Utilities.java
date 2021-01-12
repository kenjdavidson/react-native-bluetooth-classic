package kjd.reactnative.bluetooth;

import com.facebook.react.bridge.ReadableMap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Random utility classes and variables used for consistency.
 */
public class Utilities {

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static DateFormat dateFormat() {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
    }

    public static String formatDate(Date date) {
        return dateFormat().format(date);
    }

    public static Date parseDate(String date) throws ParseException {
        return dateFormat().parse(date);
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

