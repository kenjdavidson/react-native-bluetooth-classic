/**
 * Available Bluetooth events.  This replaces the events provided by the
 * NativeModule.
 */
export var BluetoothEventType;
(function (BluetoothEventType) {
    BluetoothEventType["BLUETOOTH_ENABLED"] = "BLUETOOTH_ENABLED";
    BluetoothEventType["BLUETOOTH_DISABLED"] = "BLUETOOTH_DISABLED";
    BluetoothEventType["DEVICE_CONNECTED"] = "DEVICE_CONNECTED";
    BluetoothEventType["DEVICE_DISCONNECTED"] = "DEVICE_DISCONNECTED";
    BluetoothEventType["DEVICE_READ"] = "DEVICE_READ";
    BluetoothEventType["ERROR"] = "ERROR";
    BluetoothEventType["DEVICE_DISCOVERED"] = "DEVICE_DISCOVERED";
})(BluetoothEventType || (BluetoothEventType = {}));
;
