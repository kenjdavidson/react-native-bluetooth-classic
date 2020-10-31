package kjd.reactnative.bluetooth.conn;

import kjd.reactnative.bluetooth.RNBluetoothClassicModule;

/**
 * Provides the ability to create {@link DeviceConnection}.  The factory
 * is called by the {@link RNBluetoothClassicModule}
 * during the connection request process.
 *
 * @author kendavidson
 */
public interface DeviceConnectionFactory {

    /**
     * Creates a new {@link DeviceConnection}.
     *
     * @return
     */
    DeviceConnection create();
}
