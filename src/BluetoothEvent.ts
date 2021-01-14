import { EmitterSubscription } from 'react-native';
import BluetoothNativeDevice from "./BluetoothNativeDevice";

/**
 * Available Bluetooth events.  This replaces the events provided by the 
 * NativeModule.
 */
export enum BluetoothEventType {
    BLUETOOTH_ENABLED = "BLUETOOTH_ENABLED",
    BLUETOOTH_DISABLED = "BLUETOOTH_DISABLED",
    DEVICE_CONNECTED = "DEVICE_CONNECTED",
    DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED",
    DEVICE_READ = "DEVICE_READ",
    ERROR = "ERROR",
    DEVICE_DISCOVERED = "DEVICE_DISCOVERED"
};

/**
 * BluetoothEvent wraps the message content coming from the native module.  In most cases it will
 * contain a data {string} element, although it's now possible (more easy) for applications
 * to expand upon the content.  It will be up to the developer to ensure that native and javascript
 * are using the same content type.
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
 * Device events used for connection/disconnection.  This looks like it's duplicating
 * the device and can probably be removed.
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
 * BluetoothDevice and RNBluetoothModule use event listeners for communication with javascript.
 */
export type BluetoothEventListener<T extends BluetoothEvent> = (event: T) => void;

/**
 * Used to wrap EmitterSubscription or EventSubscription 
 */
export interface BluetoothEventSubscription {
    remove(): void;
}