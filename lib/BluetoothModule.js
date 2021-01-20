var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { NativeEventEmitter, Platform, } from "react-native";
import BluetoothDevice from "./BluetoothDevice";
import { BluetoothEventType, } from "./BluetoothEvent";
global.Buffer = global.Buffer || require("buffer").Buffer;
/**
 * Provides access to native module.  In general the methods will be direct calls
 * through to {@code NativeModules.RNBluetoothClassc}, although there are instances
 * where methods are overwritten where additional information is required.  These
 * methods are related to {@code BluetoothConnection} requests, where the response
 * must be wrapped.
 *
 * @author kenjdavidson
 */
export default class BluetoothModule {
    constructor(nativeModule) {
        this._nativeModule = nativeModule;
        this._eventEmitter = new NativeEventEmitter(new NativeModule());
    }
    /**
     * Requests enabled status from the BluetoothAdapter.
     *
     * @return Promise resolved with whether Bluetooth is enabled
     */
    isBluetoothEnabled() {
        return this._nativeModule.isBluetoothEnabled();
    }
    /**
     * Requests whether there is data availabled on the Device.  At this point
     * this is a yes/no, it may be wise to turn it into number of bytes based
     * on devices that send messages of set size with no delimiter.
     *
     * @param address of the Device we wish to check
     * @return Promise resolved with whether data is available
     */
    availableFromDevice(address) {
        return this._nativeModule.availableFromDevice(address);
    }
    /**
     * Read from the specified device.  This uses the configured device read
     * functionality - see the Native documentation for how that is configured.
     *
     * @param address address from which to read
     * @return Promise resovled with individual read
     */
    readFromDevice(address) {
        return this._nativeModule.readFromDevice(address);
    }
    /**
     * Clears the device buffer.
     *
     * @param address for which device we will clear
     * @return Promise resolved with whether clear was successful
     */
    clearFromDevice(address) {
        return this._nativeModule.clearFromDevice(address);
    }
    /**
     * Disconnect from device.
     *
     * @param address of Device we will disconnect
     * @return Promise resolved with disconnection success status
     */
    disconnectFromDevice(address) {
        return this._nativeModule.disconnectFromDevice(address);
    }
    /**
     * Determines whether device is currently connected.  Connected means that there
     * is an actual Socket open (not just pairing)
     *
     * @param address of which we are checking for connection
     * @returns Promise resolved with whether there is a connection
     */
    isDeviceConnected(address) {
        return this._nativeModule.isDeviceConnected(address);
    }
    /**
     * Wraps the bonded native devices with a BluetoothDevice to allow for
     * event management.
     *
     * @return Promise containing array of pair devices
     */
    getBondedDevices() {
        return __awaiter(this, void 0, void 0, function* () {
            let bonded = yield this._nativeModule.getBondedDevices();
            let devices = [];
            for (let device of bonded) {
                devices.push(new BluetoothDevice(device, this));
            }
            return devices;
        });
    }
    /**
     * Wraps the connected native devices with a BluetoothDevice to allow for
     * event management.
     *
     * @return Promise resolved with array of connected devices
     */
    getConnectedDevices() {
        return __awaiter(this, void 0, void 0, function* () {
            let connected = yield this._nativeModule.getConnectedDevices();
            let devices = [];
            for (let device of connected) {
                devices.push(new BluetoothDevice(device, this));
            }
            return devices;
        });
    }
    /**
     * Attempts to connect to the requested device.  Defaults the properties to an empty
     * map.
     *
     * @param address the address to which we are connecting
     * @param properties extra properties required for the connection.
     */
    connectToDevice(address, options) {
        return __awaiter(this, void 0, void 0, function* () {
            // Comming from the Java world this is nuts - not being able to assign anything to
            // options because it's a <T extends StandardOptions> even with something that matches
            // the StandardOptions interface
            let connected = yield this._nativeModule.connectToDevice(address, options || {});
            return new BluetoothDevice(connected, this);
        });
    }
    /**
     * Wraps connected NativeDevice.
     *
     * @param address the address to check for connection
     */
    getConnectedDevice(address) {
        return __awaiter(this, void 0, void 0, function* () {
            let nativeDevice = yield this._nativeModule.getConnectedDevice(address);
            return new BluetoothDevice(nativeDevice, this);
        });
    }
    /**
     * Write data to the device.  Eventually this will be updated to accept data and type,
     * allowing the sending of different data elements to the device.  From the issues on
     * bluetooth-serial it seems like images and hex values are the top priorties, but method
     * to send any data would be preferable.
     *
     * @param address the address to which we will send data
     * @param message String or Buffer which will be sent
     */
    writeToDevice(address, message) {
        let data = message;
        if ("string" === typeof message) {
            data = Buffer.from(message);
        }
        else {
            data = Buffer.from(message.toString());
        }
        return this._nativeModule.writeToDevice(address, data.toString("base64"));
    }
    /**
     * Starts discovery on the bluetooth adatper.
     *
     * This is an Anroid only function.
     */
    startDiscovery() {
        return __awaiter(this, void 0, void 0, function* () {
            if (Platform.OS == "ios")
                throw new Error("Method not implemented.");
            let discoveredDevices = yield this._nativeModule.startDiscovery();
            let devices = [];
            for (let discovered of discoveredDevices) {
                devices.push(new BluetoothDevice(discovered, this));
            }
            return devices;
        });
    }
    /**
     * Cancels discovery.  If discovery was alreayd stopped, this will end gracefully
     * by resolving the promise.
     *
     * This is an Android only feature.
     */
    cancelDiscovery() {
        if (Platform.OS == "ios")
            throw new Error("Method not implemented.");
        return this._nativeModule.cancelDiscovery();
    }
    /**
     * Pair the device request.
     *
     * This is an Android only feature.
     *
     * @param address address of the device we wish to pair
     */
    pairDevice(address) {
        return __awaiter(this, void 0, void 0, function* () {
            if (Platform.OS == "ios")
                throw new Error("Method not implemented.");
            let paired = yield this._nativeModule.pairDevice(address);
            return new BluetoothDevice(paired, this);
        });
    }
    /**
     * Unpair the device request.
     *
     * This is an Android only feature.
     *
     * @param address address of the device we wish to unpair
     */
    unpairDevice(address) {
        if (Platform.OS == "ios")
            throw new Error("Method not implemented.");
        return this._nativeModule.cancelDiscovery();
    }
    /**
     * Attempt to start accepting connections.   Accepts only one connection at a time,
     * once this has been established the device is returned and accepting is disabled.
     *
     * This is an Android only feature.
     *
     * @param properties used during the connection and connected process(es)
     */
    accept(properties) {
        return __awaiter(this, void 0, void 0, function* () {
            if (Platform.OS == "ios")
                throw new Error("Method not implemented.");
            let paired = yield this._nativeModule.accept(properties);
            return new BluetoothDevice(paired, this);
        });
    }
    /**
     * Attempt to cancel the accepting state.
     *
     * This is an Android only feature.
     */
    cancelAccept() {
        if (Platform.OS == "ios")
            throw new Error("Method not implemented.");
        return this._nativeModule.cancelAccept();
    }
    /**
     * Request user to turn on Bluetooth Adapter
     *
     * This is an Android only feature.
     *
     * @param state
     */
    requestBluetoothEnabled() {
        if (Platform.OS == "ios")
            throw new Error("Method not implemented.");
        return this._nativeModule.requestBluetoothEnabled();
    }
    /**
     * Attempts to set the bluetooth adapter name.
     *
     * This is an Android only feature.
     *
     * @param name the name to which we will change BluetoothAdapter
     */
    setBluetoothAdapterName(name) {
        if (Platform.OS == "ios")
            throw new Error("Method not implemented.");
        return this._nativeModule.setBluetoothAdapterName(name);
    }
    createBluetoothEventSubscription(eventType, listener) {
        this._nativeModule.addListener(eventType);
        let subscription = this._eventEmitter.addListener(eventType, listener);
        return {
            remove: () => {
                this._nativeModule.removeListener(eventType);
                subscription.remove();
            },
        };
    }
    /**
     * Creates an EventSubscription which calls the provided listener when the native
     * device is notified of the BluetoothAdapter being enabled.
     *
     * @param listener
     */
    onBluetoothEnabled(listener) {
        return this.createBluetoothEventSubscription(BluetoothEventType.BLUETOOTH_ENABLED, listener);
    }
    /**
     * Creates an EventSubscription which calls the provided listener when the native
     * device is notified of the BluetoothAdapter being disabled.
     *
     * @param listener
     */
    onBluetoothDisabled(listener) {
        return this.createBluetoothEventSubscription(BluetoothEventType.BLUETOOTH_DISABLED, listener);
    }
    /**
     * Creates an EventSubscription which wraps both enabled and disabled.
     *
     * @param listener
     */
    onStateChanged(listener) {
        let enabledSubscription = this._eventEmitter.addListener(BluetoothEventType.BLUETOOTH_ENABLED, listener);
        let disabledSubscription = this._eventEmitter.addListener(BluetoothEventType.BLUETOOTH_DISABLED, listener);
        return {
            remove() {
                enabledSubscription.remove();
                disabledSubscription.remove();
            },
        };
    }
    /**
     * Creates an EventSubscription which wraps the DEVICE_CONNECTED event type.
     *
     * @param listener
     */
    onDeviceConnected(listener) {
        return this.createBluetoothEventSubscription(BluetoothEventType.DEVICE_CONNECTED, listener);
    }
    /**
     * Creates an EventSubscription which wraps the DEVICE_DISCONNECTED event type.  Device disconnected events
     * can be thrown for the following:
     * - During a read the DeviceConnection receives an un-cancelled exception (generally a closure)
     * - The AclReceiver receives an on disconnect (this seems less informative as it will still fire a disconnect
     * event if the connect had been cancelled.  So at this point it may need to be removed.)
     *
     * @param listener
     */
    onDeviceDisconnected(listener) {
        return this.createBluetoothEventSubscription(BluetoothEventType.DEVICE_DISCONNECTED, listener);
    }
    /**
     * Creates an EventSubscription based on the read event from a specified device.  If the device
     * is not currently connected an exception will be thrown, although I'm not sure if
     * this is required, since it may be annoying to continually add/remove subscriptions.
     *
     * @param listener
     */
    onDeviceRead(address, listener) {
        let eventType = `${BluetoothEventType.DEVICE_READ}@${address}`;
        this._nativeModule.addListener(eventType);
        let subscription = this._eventEmitter.addListener(eventType, listener);
        return {
            remove() {
                subscription.remove();
            },
        };
    }
    /**
     * Creates an EventSubscription which wraps the ERROR event.
     *
     * @param listener
     */
    onError(listener) {
        return this.createBluetoothEventSubscription(BluetoothEventType.ERROR, listener);
    }
    /**
     * Creates an event subscription wrapping the DEVICE_DISCOVERED events.  DEVICE_DISCOVERED is fired during the
     * discovery process, when a new device is found.  Note this is only fired on the first discovery, it will not
     * be fired (at this point) with the updated RSSI value on the next device discovery.
     *
     * Remember to remove the subscription when you've found your device, or you stop discovery.
     *
     * @param listener
     */
    onDeviceDiscovered(listener) {
        return this.createBluetoothEventSubscription(BluetoothEventType.DEVICE_DISCOVERED, listener);
    }
}
/**
 * Internal `NativeModule` to get around the fact that React doesn't actually make this
 * type available, but we need it in order to create our BluetoothModule.
 */
class NativeModule {
    addListener(eventType) { }
    removeListeners(count) { }
}
