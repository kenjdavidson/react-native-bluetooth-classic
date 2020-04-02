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
}
