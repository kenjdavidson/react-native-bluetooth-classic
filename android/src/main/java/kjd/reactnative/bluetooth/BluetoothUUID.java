package kjd.reactnative.bluetooth;

import java.util.UUID;

public enum BluetoothUUID {
    SPP(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

    public final UUID uuid;
    BluetoothUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
