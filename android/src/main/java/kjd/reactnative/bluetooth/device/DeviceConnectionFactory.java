package kjd.reactnative.bluetooth.device;

import kjd.reactnative.RNBluetoothClassicModule;

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
