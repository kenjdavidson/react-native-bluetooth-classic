import BluetoothModule from "./BluetoothModule";
import BluetoothNativeDevice from "./BluetoothNativeDevice";
import { BluetoothEvent, BluetoothEventListener, BluetoothDeviceReadEvent, BluetoothEventSubscription } from "./BluetoothEvent";

export default class BluetoothDevice implements BluetoothNativeDevice {
	private _bluetoothModule: BluetoothModule;
	private _nativeDevice: BluetoothNativeDevice;

	name: string;
	address: string;
	id: string;
	bonded?: Boolean;
	deviceClass?: string;
	rssi: Number;
	extra: Map<string, Object>;

  constructor(
    nativeDevice: BluetoothNativeDevice,
    bluetoothModule: BluetoothModule
  ) {
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
	
	async connect(options: Map<string, object>): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			try {
				let connected = await this._bluetoothModule.connectToDevice(this.address, options);
				resolve(!!connected)
			} catch (err) {
				reject(err);
			}			
		});
	}

	isConnected(): Promise<boolean> {
		return this._bluetoothModule.isDeviceConnected(this.address);
	}
	
	disconnect(): Promise<boolean> {
		return this._bluetoothModule.disconnectFromDevice(this.address);
	}
	
	async write(data: any): Promise<boolean> {
		throw new Error("Method not implemented.");
	}
	
	onDataReceived(listener: BluetoothEventListener<BluetoothDeviceReadEvent>): BluetoothEventSubscription {
		return this._bluetoothModule.onDeviceRead(this.address, listener);
	}

}
