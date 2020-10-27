import BluetoothModule from "./BluetoothModule";
import BluetoothNativeDevice from "./BluetoothNativeDevice";
import { BluetoothEventListener, BluetoothDeviceReadEvent, BluetoothEventSubscription } from "./BluetoothEvent";
export default class BluetoothDevice implements BluetoothNativeDevice {
    private _bluetoothModule;
    private _nativeDevice;
    name: string;
    address: string;
    id: string;
    bonded?: Boolean;
    deviceClass?: string;
    rssi: Number;
    extra: Map<string, Object>;
    constructor(nativeDevice: BluetoothNativeDevice, bluetoothModule: BluetoothModule);
    connect(options: Map<string, object>): Promise<boolean>;
    isConnected(): Promise<boolean>;
    disconnect(): Promise<boolean>;
    write(data: any): Promise<boolean>;
    onDataReceived(listener: BluetoothEventListener<BluetoothDeviceReadEvent>): BluetoothEventSubscription;
}
