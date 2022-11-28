package kjd.reactnative.bluetooth;

import java.util.UUID;

public enum BluetoothUUID {
    SPP(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));

    public final UUID uuid;
    BluetoothUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
