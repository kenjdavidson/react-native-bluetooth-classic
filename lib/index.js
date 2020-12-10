import { NativeModules } from "react-native";
import BluetoothDevice from "./BluetoothDevice";
import BluetoothError from "./BluetoothError";
import { BluetoothEventType, } from "./BluetoothEvent";
import BluetoothModule from "./BluetoothModule";
export default new BluetoothModule(NativeModules.RNBluetoothClassic);
export { BluetoothDevice, BluetoothError, BluetoothEventType };
