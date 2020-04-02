package kjd.reactnative.bluetooth;

import kjd.reactnative.bluetooth.device.NativeDevice;

public class DevicePairingException extends BluetoothException {

    public DevicePairingException(NativeDevice device, Throwable cause) {
        super(device, Exceptions.PAIRING_FAILED.message(device.getAddress()), cause);
    }

}
