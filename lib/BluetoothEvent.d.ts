import BluetoothNativeDevice from "./BluetoothNativeDevice";
/**
 * Available Bluetooth events.  This replaces the events provided by the
 * NativeModule.
 */
export declare enum BluetoothEventType {
    BLUETOOTH_ENABLED = "BLUETOOTH_ENABLED",
    BLUETOOTH_DISABLED = "BLUETOOTH_DISABLED",
    DEVICE_CONNECTED = "DEVICE_CONNECTED",
    DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED",
    DEVICE_READ = "DEVICE_READ",
    ERROR = "ERROR"
}
/**
 * BluetoothEvent
 */
export interface BluetoothEvent {
    device: BluetoothNativeDevice;
    eventType: BluetoothEventType;
    timestamp: string;
}
/**
 * State change event used for enable/disable
 */
export interface StateChangeEvent extends BluetoothEvent {
    state: string;
    enabled: boolean;
}
/**
 * Device events used for connection/disconnection
 */
export interface BluetoothDeviceEvent extends BluetoothEvent {
    device: BluetoothNativeDevice;
}
/**
 * Device read events.
 */
export interface BluetoothDeviceReadEvent extends BluetoothDeviceEvent {
    data: string;
}
/**
 * Event listener
 */
export declare type BluetoothEventListener<T extends BluetoothEvent> = (event: T) => undefined;
/**
 * Used to wrap EmitterSubscription or EventSubscription
 */
export interface BluetoothEventSubscription {
    remove(): void;
}
