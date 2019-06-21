
import { Platform, NativeModules, DeviceEventEmitter, NativeEventEmitter } from 'react-native';
import { Buffer } from 'buffer';

/**
 * Simulates the EmitterSubscription for Android, allowing the same functionality to be used without
 * worrying about Platform.  I need to review a little more, and see if Android actually does this
 * by default now, but it seems to be working.  In both cases, the usage is:
 * 
 * this.subscription = RNBluetoothClassic.addListener(eventName, handler, this);
 * this.subscription.remove();  // when complete
 */
class DeviceEventSubscription {
  constructor(eventName, handler) {
    this.eventName = eventName;
    this.handler = handler;

    DeviceEventEmitter.addListener(eventName, handler);
  }

  remove = () => DeviceEventEmitter.removeListener(this.eventName, this.handler);
}

/**
 * As per the documentation the NativeEventEmitter is used for IOS event subscriptions.  Extending
 * this way provides IntelliSense and added control - at the cost of more typing if any functionality
 * is added or removed. 
 * 
 * I've done (and will do) my best to maintain as much code between Android and IOS as possible, removing
 * the requirement for any Platform.OS logic within the React Native components.  If there are any times
 * when this isn't possible, it'll be well documented.
 * 
 * TODO look into which method is better this.function = nativeModule.function or 
 * function = (...) => nativeModule.function(...).  At this point it all depends on what makes VSCode
 * more standable when using the library.
 */
class RNBluetoothClassic extends NativeEventEmitter {
  constructor(nativeModule) {
    super(nativeModule)

    this.requestEnable = nativeModule.requestEnable;
    this.isEnabled = nativeModule.isEnabled;
    this.list = nativeModule.list;
    this.connect = nativeModule.connect;
    this.disconnect = nativeModule.disconnect;
    this.writeToDevice = nativeModule.writeToDevice;
  }

  /**
   * Override the NativeEventEmitter#addListener method providing functionality for 
   * Android.  I felt this was important as I didn't want clients to have to determine
   * which platform to use the event listening features of React.
   * 
   * @param {string} eventName to which the listener will be attached
   * @param {function} handler which will be called on event
   * @param {object} context optional context object of the listener
   */
  addListener = (eventName, handler, context) => {
    if (Platform.OS === 'ios') {
      return super.addListener(eventName, handler, context);
    } else {      
      return new DeviceEventSubscription(eventName, handler);
    }
  }

  /**
   * Remove all the listeners for an eventName.
   * 
   * @param {string} eventName which will have all it's listeners removed
   */
  removeAllListeners = (eventName) => {
    if (Platform.OS === 'ios') {
      super.removeAllListeners(eventName);
    } else {      
      DeviceEventEmitter.removeAllListeners(eventName);      
    }
  } 

  /**
   * Write data to the device.  Eventually this will be updated to accept data and type, 
   * allowing the sending of different data elements to the device.  From the issues on
   * bluetooth-serial it seems like images and hex values are the top priorties, but method
   * to send any data would be preferable.
   * 
   * @param {string|buffer} data to be sent to the device as base64 string 
   * 
   * TODO modify for byte[] instead of string
   */
  write = (data) => {
    if (typeof data === 'string') {
      data = new Buffer(data);
    }
    this._nativeModule.writeToDevice(data.toString('base64'));
  }
}

export const BTEvents = Platform.OS === 'ios' 
    ? NativeModules.RNBluetoothClassic.BTEvents // .getConstants() should actually work here
    : NativeModules.RNBluetoothClassic.getConstants().BTEvents;   
 
export default new RNBluetoothClassic(NativeModules.RNBluetoothClassic);
