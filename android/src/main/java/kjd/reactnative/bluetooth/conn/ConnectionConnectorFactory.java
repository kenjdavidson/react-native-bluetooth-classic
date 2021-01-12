package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.util.Properties;

public interface ConnectionConnectorFactory {
    ConnectionConnector create(BluetoothDevice device, Properties properties) throws IOException;
}
