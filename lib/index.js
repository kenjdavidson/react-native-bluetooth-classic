import { NativeModules } from 'react-native';
import BluetoothModule from './BluetoothModule';
import BluetoothDevice from './BluetoothDevice';
import BluetoothErrors from './BluetoothErrors';
import BluetoothEvents from './BluetoothEvents';
export default new BluetoothModule(NativeModules.RNBluetoothClassic);
export { BluetoothDevice, BluetoothEvents, BluetoothErrors };
