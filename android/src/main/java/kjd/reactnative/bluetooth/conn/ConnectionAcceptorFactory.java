package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothAdapter;

import java.io.IOException;
import java.util.Properties;

public interface ConnectionAcceptorFactory{
    ConnectionAcceptor create(BluetoothAdapter adapter, Properties properties) throws IOException;
}
