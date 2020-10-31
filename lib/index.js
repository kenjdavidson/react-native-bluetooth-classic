import { NativeModules } from 'react-native';
import BluetoothModule from './BluetoothModule';
export default new BluetoothModule(NativeModules.RNBluetoothClassic);
export * from "./BluetoothEvent";
export * from "./BluetoothError";
export * from "./BluetoothDevice";
