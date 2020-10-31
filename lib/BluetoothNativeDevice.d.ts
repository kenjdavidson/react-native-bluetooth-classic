/**
 * kjd.reactnative.bluetooth.device.NativeDevice
 *
 * @author kenjdavidson
 *
 */
export default interface BluetoothNativeDevice {
    /**
     * Name of the device - address if no name
     */
    name: string;
    /**
     * Physical address (MAC) of the device.
     */
    address: string;
    /**
     * Identifier of the device.  On Android this is the Address
     */
    id: string;
    /**
     * Bonded state
     */
    bonded?: Boolean;
    /**
     * Device class
     */
    deviceClass?: string;
    /**
     * RSSI value of the connection
     */
    rssi: Number;
    /**
     * Extra information.  This could contain things like RSSI value
     * or other details.
     */
    extra: Map<string, Object>;
}
