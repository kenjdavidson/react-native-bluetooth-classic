import { NativeModules } from "react-native";
import BluetoothDevice from "./BluetoothDevice";
import BluetoothError from "./BluetoothError";
import {
  BluetoothEvent,
  BluetoothDeviceEvent,
  BluetoothDeviceReadEvent,
  BluetoothEventListener,
  BluetoothEventSubscription,
  BluetoothEventType,
} from "./BluetoothEvent";
import BluetoothModule from "./BluetoothModule";
import BluetoothNativeDevice from "./BluetoothNativeDevice"
import BluetoothNativeModule, { StandardOptions } from "./BluetoothNativeModule";

export default new BluetoothModule(NativeModules.RNBluetoothClassic);

export {
  BluetoothDevice,
  BluetoothError,
  BluetoothEvent,
  BluetoothDeviceEvent,
  BluetoothDeviceReadEvent,
  BluetoothEventListener,
  BluetoothEventSubscription,
  BluetoothEventType,
  BluetoothNativeDevice,
  BluetoothNativeModule,
  StandardOptions
};
