import { NativeModules } from '../node_modules/react-native/types/index';
import BluetoothDevice from './BluetoothDevice';
import BluetoothError from './BluetoothError';
import {
  BluetoothEvent,
  BluetoothDeviceEvent,
  BluetoothDeviceReadEvent,
  BluetoothEventListener,
  BluetoothEventSubscription,
  BluetoothEventType,
} from './BluetoothEvent';
import BluetoothModule from './BluetoothModule';
import BluetoothNativeDevice from './BluetoothNativeDevice';
import BluetoothNativeModule, { StandardOptions } from './BluetoothNativeModule';

export default new BluetoothModule(NativeModules.RNBluetoothClassic);

export type {
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
  StandardOptions,
};
