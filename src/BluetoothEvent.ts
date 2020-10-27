import { EmitterSubscription } from 'react-native';
import BluetoothNativeDevice from "../lib/BluetoothNativeDevice";

export enum BluetoothEventType {
    BLUETOOTH_ENABLED = "BLUETOOTH_ENABLED",
    BLUETOOTH_DISABLED = "BLUETOOTH_DISABLED",
    DEVICE_CONNECTED = "DEVICE_CONNECTED",
    DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED",
    DEVICE_READ = "DEVICE_READ",
    ERROR = "ERROR"
};

/**
 * Standard bluetooth event
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
export type BluetoothEventListener<T extends BluetoothEvent> = (event: T) => undefined;

/**
 * Used to wrap EmitterSubscription or EventSubscription 
 */
export interface BluetoothEventSubscription {
    remove(): void;
}