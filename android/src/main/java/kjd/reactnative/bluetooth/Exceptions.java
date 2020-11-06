package kjd.reactnative.bluetooth;

import android.util.JsonWriter;

public enum Exceptions {
    BLUETOOTH_NOT_ENABLED("Bluetooth mAdapter is not enabled"),
    BLUETOOTH_IN_DISCOVERY("Bluetooth already in discovery mode"),
    BLUETOOTH_IN_ACCEPTING("Bluetooth already in accepting state"),
    BLUETOOTH_NOT_ACCEPTING("Bluetooth is not currently accepting"),
    ALREADY_CONNECTING("Already attempting connection to device %s"),
    ALREADY_CONNECTED("Already connected to device %s"),
    NOT_CURRENTLY_CONNECTED("Not connected to %s"),
    BONDING_UNAVAILABLE_API("Bluetooth bonding is unavailable in this version of Android"),
    DISCOVERY_FAILED("Attempt to discover failed due to: %s"),
    WRITE_FAILED("Unable to write to device, due to: %s"),
    READ_FAILED("Unable to read from device, due to: %s"),
    ACCEPTING_CANCELLED("Accept was cancelled"),
    CONNECTION_FAILED("Connection to %s failed."),
    CONNECTION_LOST("Connection to %s was lost"),
    PAIRING_FAILED("Unable to complete pairing with %s")
    ;

    private final String message;
    Exceptions(String message) {
        this.message = message;
    }

    public String message(Object... args) {
        return String.format(message, args);
    }
}
