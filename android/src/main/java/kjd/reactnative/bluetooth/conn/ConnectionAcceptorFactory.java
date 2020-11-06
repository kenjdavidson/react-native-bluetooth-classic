package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothAdapter;

import java.io.IOException;
import java.util.Properties;

import kjd.reactnative.bluetooth.RNBluetoothClassicModule;

public interface ConnectionAcceptorFactory{
    ConnectionAcceptor create(BluetoothAdapter adapter, Properties properties) throws IOException;
}
