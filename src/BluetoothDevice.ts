import BluetoothModule from "./BluetoothModule";
import BluetoothNativeDevice from "./BluetoothNativeDevice";

export default class BluetoothDevice implements BluetoothNativeDevice {
	_bluetoothModule: BluetoothModule;
	_nativeDevice: BluetoothNativeDevice;

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
}
