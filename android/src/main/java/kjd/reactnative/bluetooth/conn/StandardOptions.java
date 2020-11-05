package kjd.reactnative.bluetooth.conn;

import java.nio.charset.Charset;

/**
 * Standard options used for initiating and managing {@link DeviceConnection}(s).
 */
public enum StandardOptions {
    DELIMITER("delimiter", "\n"),
    DEVICE_CHARSET("charset", Charset.forName("ascii")),
    READ_TIMEOUT("read_timeout", 300),
    READ_SIZE("read_size", 1024),
    SECURE_SOCKET("secure", true),
    SERVICE_NAME("service_name", "RNBluetoothClassic")
    ;

    private String code;
    private Object defaultValue;

    StandardOptions(String code, Object defaultValue) {
        this.code = code;
        this.defaultValue = defaultValue;
    }

    public String code() {
        return code;
    }

    public <T> T defaultValue() {
        return (T) defaultValue;
    }
}
