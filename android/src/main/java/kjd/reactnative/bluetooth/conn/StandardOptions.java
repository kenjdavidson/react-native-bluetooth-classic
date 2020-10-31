package kjd.reactnative.bluetooth.conn;

import java.nio.charset.Charset;

/**
 * Standard options used for initiating and managing {@link DeviceConnection}(s).
 */
public enum StandardOptions {
    Delimiter("delimiter", "\n"),
    DeviceCharset("charset", Charset.forName("ascii")),
    ReadTimeout("read_timeout", 300),
    ReadSize("read_size", 1024),
    Secure("secure", true),
    ServiceName("service_name", "RNBluetoothClassic")
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
