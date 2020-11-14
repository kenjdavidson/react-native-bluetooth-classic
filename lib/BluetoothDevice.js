var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
/**
 * Implements the BluetoothNativeDevice which is used to communicate with the Android
 * and IOS native module.  Provides access to the BluetoothDevice (Android) and
 * EAAccessory (IOS) details as well as configuration of listeners.
 *
 * @author kendavidson
 */
export default class BluetoothDevice {
    constructor(nativeDevice, bluetoothModule) {
        this._bluetoothModule = bluetoothModule;
        this._nativeDevice = nativeDevice;
        this.name = nativeDevice.name;
        this.address = nativeDevice.address;
        this.id = nativeDevice.id;
        this.bonded = nativeDevice.bonded;
        this.deviceClass = nativeDevice.deviceClass;
        this.rssi = nativeDevice.rssi;
        this.extra = nativeDevice.extra;
    }
    /**
     * Attempts to open a BluetoothSocket (Android) or Stream (IOS) with the device.  When this
     * is completed successfully the device is said to be **connected**, otherwise the device
     * is referred to as **bonded**
     *
     * @param options 	used to perform connetion and communication.  This is currently a generic
     * 					map based on the native implementation of the RNBluetoothClassic module,
     * 					DeviceConnector and DeviceConnection.
     * @return Promise resolving true|false whether the connetion was established
     */
    connect(options) {
        return __awaiter(this, void 0, void 0, function* () {
            return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
                try {
                    let connected = yield this._bluetoothModule.connectToDevice(this.address, options);
                    resolve(!!connected);
                }
                catch (err) {
                    reject(err);
                }
            }));
        });
    }
    /**
     * Determine whether the device is currently connected.   Again it's important to note that
     * **connected** means that there is an active BluetoothSocket/Stream available.
     *
     * @return Promise resolving true|false based on connection status
     */
    isConnected() {
        return __awaiter(this, void 0, void 0, function* () {
            return this._bluetoothModule.isDeviceConnected(this.address);
        });
    }
    /**
     * Disconnect from the device.
     *
     * @return Promise resolving true|false whether disconnection was successful
     */
    disconnect() {
        return __awaiter(this, void 0, void 0, function* () {
            return this._bluetoothModule.disconnectFromDevice(this.address);
        });
    }
    /**
     * How many bytes/messages are available.  This depends completely on the implementation
     * of the DeviceConnection.  The standard implementation is based on delimited String(s)
     * so this will return the number of messages available for reading.
     *
     * @return Promise resolving the number of messages/data available
     */
    available() {
        return __awaiter(this, void 0, void 0, function* () {
            return this._bluetoothModule.availableFromDevice(this.address);
        });
    }
    /**
     * Read an individual message/data package from the device.  This depends completely on the
     * implementation of DeviceConnection.  The standard implemenation is based on delimited
     * String(s) so this will return 1 delimtied message.
     *
     * @return Promise resolved with the message content (not including delimited)
     */
    read() {
        return __awaiter(this, void 0, void 0, function* () {
            return this._bluetoothModule.readFromDevice(this.address);
        });
    }
    /**
     * Writes the provided data to the device.  This accepts String or Buffer data, if String
     * it will be converted to a Buffer and then Base64 encoded prior to sending to the
     * Native module.
     *
     * @param data to be written to the device.
     */
    write(data) {
        return __awaiter(this, void 0, void 0, function* () {
            return this._bluetoothModule.writeToDevice(this.address, data);
        });
    }
    /**
     * Adds a listener to the device.  Once completed this will:
     * - send queued data already read from the device (if implemented by DeviceConnection)
     * - send all subsequent data
     *
     * @param listener the BluetoothEventListener which will receive incoming data
     */
    onDataReceived(listener) {
        return this._bluetoothModule.onDeviceRead(this.address, listener);
    }
}
