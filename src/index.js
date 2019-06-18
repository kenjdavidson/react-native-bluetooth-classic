
import { Platform, NativeModules, DeviceEventEmitter } from 'react-native';
import { Buffer } from 'buffer';

const RNBluetoothClassic = NativeModules.RNBluetoothClassic;
const NOOP = () => {};

export const BTEvents = Platform.OS === 'ios' ? RNBluetoothClassic.BTEvents : RNBluetoothClassic.getConstants().BTEvents;

/**
 * Listen for the specified event.
 * 
 * TODO if the event is not allowed (check get default constants eventNames) then error
 * 
 * @param {string} eventName
 * @param {function} handler
 */
RNBluetoothClassic.addListener = (eventName, handler) => DeviceEventEmitter.addListener(eventName, handler);

/**
 * Remove the specified event handler.
 * 
 * @param {string} eventName
 * @param {function} handler
 */
RNBluetoothClassic.removeListener = (eventName, handler) => DeviceEventEmitter.removeListener(eventName, handler);

/**
 * Remove all listeners
 */
RNBluetoothClassic.removeAllListeners = () => DeviceEventEmitter.removeAllListeners();

/**
 * Writes data - first converting
 * 
 * @param {string} data
 * @return Promise 
 */
RNBluetoothClassic.write = (data) => {
  if (typeof data === 'string') {
    data = new Buffer(data)
  }
  return BluetoothSerial.writeToDevice(data.toString('base64'))
}

export default RNBluetoothClassic;
