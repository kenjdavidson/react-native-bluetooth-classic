var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
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
    isConnected() {
        return this._bluetoothModule.isDeviceConnected(this.address);
    }
    disconnect() {
        return this._bluetoothModule.disconnectFromDevice(this.address);
    }
    write(data) {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error("Method not implemented.");
        });
    }
    onDataReceived(listener) {
        return this._bluetoothModule.onDeviceRead(this.address, listener);
    }
}
