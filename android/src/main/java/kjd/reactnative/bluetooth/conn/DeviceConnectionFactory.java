package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Properties;

@FunctionalInterface
public interface DeviceConnectionFactory {
    DeviceConnection create(BluetoothSocket device, Properties properties) throws IOException;
}
