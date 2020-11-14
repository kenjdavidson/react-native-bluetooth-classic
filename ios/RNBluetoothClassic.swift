//
//  RNBluetoothClassic.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import ExternalAccessory
import CoreBluetooth

/**
 Implementation of the RNBluetoothClassic React Native module.  For information on how this
 module was created and developed see the following:
 
 - https://facebook.github.io/react-native/docs/native-modules-setup
 - https://facebook.github.io/react-native/docs/native-modules-ios
 
 or the README.md located in the parent (Javascript) project.
 
 RNBluetoothClassic is responsible for interacting with the ExternalAccessory framework
 and providing wrappers for listing, connecting, reading, writing, etc.  The actual
 communication has been handed off to the BluetoothDevice class - allowing (in the future)
 more that one BluetoothDevice to be connected at one time.
 
 Currently the module communicates using Base64 .utf8 encoded strings.  This should
 be updated in the future to use [UInt8] to match the communication on the
 BluetoothDevice side.  This means that the responsiblity of converting and managing
 data is done in Javascript/client rather than in the module.
 */
@objc(RNBluetoothClassic)
class RNBluetoothClassic : NSObject, RCTBridgeModule {
    
    static func moduleName() -> String! {
        return "RNBluetoothClassic"
    }
    
    @objc var bridge: RCTBridge!
    var connectionFactories: Dictionary<String,DeviceConnectionFactory>
    
    private let eaManager: EAAccessoryManager
    private let cbCentral: CBCentralManager
    private let notificationCenter: NotificationCenter
    private let supportedProtocols: [String]
    
    private var listeners: Dictionary<String,Int>
    private var connections: Dictionary<String,DeviceConnection>
    
    /**
     * Initializes the RNBluetoothClassic module.  At this point it's not quite as customizable as the
     * Java version, but I'm slowly working on figuring out how to incorporate the same logic in a
     * Swify way, but my ObjC and Swift is not strong, very very not strong.
     */
    override init() {
        self.eaManager = EAAccessoryManager.shared()
        self.cbCentral = CBCentralManager()
        self.notificationCenter = NotificationCenter.default
        self.supportedProtocols = Bundle.main
            .object(forInfoDictionaryKey: "UISupportedExternalAccessoryProtocols") as! [String]
        
        self.connectionFactories = Dictionary()
        self.connectionFactories["delimited"] = DelimitedStringDeviceConnectionFactory()
        
        self.connections = Dictionary()
        self.listeners = Dictionary()
        
        super.init()
        
        self.registerForLocalNotifications()
    }
    
    /**
     Clean up:
     - Notifications
     - Observers
     - Any other objects that need to be manually released
     */
    deinit {
        unregisterForLocalNotifications()
    }
    
    /**
     Register with the NotificationCenter and add all appropriate Observers.  Currently the available
     notification types are:
     - .EAAccessoryDidConnect = BTEvent.BLUETOOTH_CONNECTED
     - .EAAccessoryDidDisconnect = BTEvent.BLUETOOTH_DISCONNECTED
     using the appropriate BTEvent type(s)
     */
    private func registerForLocalNotifications() {
        eaManager.registerForLocalNotifications()
        notificationCenter.addObserver(self,
                                       selector: #selector(accessoryDidConnect),
                                       name: .EAAccessoryDidConnect,
                                       object: nil)
        notificationCenter.addObserver(self,
                                       selector: #selector(accessoryDidDisconnect),
                                       name: .EAAccessoryDidDisconnect,
                                       object: nil)
    }
    
    /**
     Remove all Observers and unregister with the NotificationCenter
     */
    private func unregisterForLocalNotifications() {
        notificationCenter.removeObserver(self)
        eaManager.unregisterForLocalNotifications()
    }
    
    /**
     * Implements the EAAccessoryDidConnect delegate observer.  Fires a DeviceConnected event
     * to react native.  In terms of IOS connection this means that the perfipheral is ON and BONDED
     * (which IOS calls connected, the device might actually show connected) but this does not mean
     * that there is an active socket/stream open
     */
    @objc
    func accessoryDidConnect(_ notification:Notification) {
        // Unlike the disconnect we just need to pass the event to the application.  It
        // will decide whether or not to connect.
        if let connected: EAAccessory = notification.userInfo!["EAAccessoryKey"] as? EAAccessory {
            sendEvent(EventType.DEVICE_CONNECTED.name,
                      body: NativeDevice(accessory: connected).map())
        }
    }
    
    /**
     * Received a disconnct notification from IOS.  If we are currently connected to this device, we need to disconnect it
     * and remove it from the connected peripherals map. In terms of IOS connection this means that the perfipheral
     * is OFF and BONDED (which IOS calls connected, the device might actually show connected) but this does not mean
     * that there is an active socket/stream open
     */
    @objc
    func accessoryDidDisconnect(_ notification:Notification) {
        if let disconnected: EAAccessory = notification.userInfo!["EAAccessoryKey"] as? EAAccessory {
            // If we are currently connected to this, then we need to
            // disconnected it and remove the current peripheral
            if let currentDevice = connections.removeValue(forKey: disconnected.serialNumber) {
                currentDevice.disconnect()
            }
            
            // Finally send the notification
            sendEvent(EventType.DEVICE_DISCONNECTED.name,
                      body: NativeDevice(accessory: disconnected).map())
        }
    }
    
    /**
     RCTEventEmitter -
     Turned off the main queue setup for now - testing.  But this will need to be turned
     on as the ExternalAccessory event handling needs to occur on a separate thread and
     be haneled correctly.
     */
    static func requiresMainQueueSetup() -> Bool {
        return true;
    }
    
    /**
     RCTEventEmitter -
     Return the constants for BTEvents and BTCharsets specific to IOS.
     */
    func constantsToExport() -> [AnyHashable : Any]! {
        return [:];
    }
    
    /**
     * Whether or not bluetooth is currently enabled - currently this is done by using the
     * CoreBluetooth (BLE) framework, as it should hopefully be good enough for performing
     * bluetooth system tasks.
     * - parameter resolver: resovles with true|false based on enable
     * - parameter reject: should never be rejected
     */
    @objc
    func isBluetoothEnabled(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        resolve(checkBluetoothAdapter())
    }
    
    /**
     * Check the Core Bluetooth Central Manager for status
     */
    private func checkBluetoothAdapter() -> Bool {
        var enabled = false
        
        if #available(iOS 10.0, *) {
            enabled = (cbCentral.state == CBManagerState.poweredOn)
        } else {
            enabled = (cbCentral.state.rawValue == CBCentralManagerState.poweredOn.rawValue)
        }
        
        return enabled
    }
    
    private func rejectBluetoothDisabled(rejecter reject: RCTPromiseRejectBlock) {
        let error = BluetoothError.BLUETOOTH_DISABLED
        reject(error.info.abbr, error.info.message, error.error)
    }
    
    /**
     * Lists currently connected/bonded devices - devices must have matching protocols
     * to those configured in the .plist UISupportedExternalAccessoryProtocols key.
     * The call should never be rejected, only resolved with an empty list.  Uses
     * the EAAccessoryManager.shared().connectedAccessories to get the devices.
     * - parameter resolver: resovles with the list (possibly empty)
     * - parameter reject: should never be rejected
     */
    @objc
    func getBondedDevices(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        var accessories:[NSDictionary] = [NSDictionary]()
        for connected in eaManager.connectedAccessories {
            let device = NativeDevice(accessory: connected)
            accessories.append(device.map())
        }
        resolve(accessories)
    }

    /**
     * Determines whether the device is still connected and attempts to open
     * a connection to it.  This is done by providing the self to the BluetoothDevice
     * as a delegate during the open request.  If the device cannot be connected to,
     * fails connection or bluetooth is just not enabled, then the request
     * is rejected.
     * - parameter _: the device Id/address in which to connect
     * - parameter resolve: resolve when the connection has been established
     * - parameter reject: reject a failed connection
     */
    @objc
    func connectToDevice(
        _ deviceId: String,
        options: NSDictionary,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        guard connections[deviceId] == nil else {
            let connection = connections[deviceId]!
            resolve(NativeDevice(accessory: connection.accessory).map())
            return
        }
        
        var connectionOptions = Dictionary<String,Any>()
        connectionOptions.merge(options as! [String : Any]) { $1 }
        
        let connectionType = connectionOptions["CONNECTION_TYPE"] ?? "delimited";
        guard let factory = connectionFactories[connectionType as! String] else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 200)
            reject("invalid_connection_type", "Invalid connection type", error)
            return;
        }

        // Now check to see that the device is still connected and available
        // using the EAAccessoryManager, if found we create a new BluetoothDevice
        // which will be responsible for managing our connection
        if let accessory = eaManager.connectedAccessories.first(where: { $0.serialNumber == deviceId }) {
            if let protocolString:String = determineProtocolString(forDevice: accessory) {
                connectionOptions["PROTOCOL_STRING"] = protocolString
                
                NSLog("(RNBluetoothClassic:connect) Connecting to %@ with %@", accessory.name, protocolString)
                let connection = factory.create(accessory: accessory, options: connectionOptions)
                
                do {
                    try connection.connect()
                    
                    self.connections[deviceId] = connection
                    resolve(NativeDevice(accessory: accessory).map())
                } catch {
                    let error = BluetoothError.CONNECTION_FAILED
                    reject(error.info.abbr, error.info.message, error.error)
                }
            } else {
                let error = NSError(domain: "kjd.reactnative.bluetooth", code: 201)
                reject("connect_failed", "Device could not establish connection", error)
            }
        } else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 202)
            reject("device_not_found", "Device is not currently bonded/paired", error)
        }
    }
    
    private func determineProtocolString(forDevice accessory:EAAccessory) -> String? {
        return supportedProtocols.first(where: {
            accessory.protocolStrings.contains($0)
        })
    }
    
    /**
     Disconnect from the currently connected device.
     - parameter _: the device Id/address from which we will disconnect
     - parameter resolver: resolve the disconnection
     - parameter reject: reject if the disconnection fails
     */
    @objc
    func disconnectFromDevice(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        guard let connected = connections.removeValue(forKey: deviceId) else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 203)
            reject("device_not_connected", "Device is not currently connected", error)
            return
        }
        
        NSLog("(RNBluetoothClassic:disconnect) Disconnecting %@", connected.accessory.name)
        connected.disconnect()
        resolve(true)
    }
    
    /**
     Determine whether there is a connected device
     - parameter _: device Id to check for connection
     - parameter resolver: resolve with the whether the device is connected
     - parameter rejecter: reject if Bluetooth is disabled or there are any issues.
     */
    @objc
    func isDeviceConnected(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        resolve(connections[deviceId] != nil)
    }
    
    /**
     Resolve the connected device.
     - parameter _: device Id to check for connection
     - parameter resolver: resolve with the whether the device is connected
     - parameter rejecter: reject if Bluetooth is disabled or there are any issues.
     */
    @objc
    func getConnectedDevice(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        guard let connected = connections[deviceId] else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 203)
            reject("device_not_connected", "Device is not currently connected", error)
            return
        }
        
        resolve(NativeDevice(accessory: connected.accessory).map())
    }
    
    /**
     Writes the supplied message to the device - the message should be Base64
     encoded.
     - parameter _: device Id to check for connection
     - parameter message: the message to send
     - parameter resolver: resolve with the whether the device is connected
     - parameter rejecter: reject if Bluetooth is disabled or there are any issues.
     */
    @objc
    func writeToDevice(
        _ deviceId: String,
        withMessage message: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        guard let connected = connections[deviceId] else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 203)
            reject("device_not_connected", "Device is not currently connected", error)
            return
        }
        
        if let decoded = Data(base64Encoded: message) {
            resolve(connected.write(decoded))
        } else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 204)
            reject("cannot_decode_data", "Cannot decode data", error)
        }
    }
    
    /**
     Attempts to read all of the data from the buffer, ignoring the delimiter.  If no
     data is in the buffer, an empty String will be returned.
     - parameter _: device Id to check for connection
     - parameter resolver: resolve with the whether the device is connected
     - parameter rejecter: reject if Bluetooth is disabled or there are any issues.
     */
    @objc
    func readFromDevice(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        guard let connected = connections[deviceId] else {
           let error = NSError(domain: "kjd.reactnative.bluetooth", code: 203)
           reject("device_not_connected", "Device is not currently connected", error)
            return
        }
        
        resolve(connected.read())
    }
    
    /**
     Clear the buffer.
     - parameter _: device Id to check for connection
     - parameter resolver: resolve with the whether the device is connected
     - parameter rejecter: reject if Bluetooth is disabled or there are any issues.
     */
    @objc
    func clearFromDevice(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        guard let connected = connections[deviceId] else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 203)
            reject("device_not_connected", "Device is not currently connected", error)
            return
        }
       
        connected.clear()
        resolve(true)
    }
    
    /**
     Resolves with the amount of data available to be read.  This is the total
     buffer length, with no regard for the delimiter.  Should possibly add in
     a delimiter value.
     - parameter _: device Id to check for connection
     - parameter resolver: resolve with the whether the device is connected
     - parameter rejecter: reject if Bluetooth is disabled or there are any issues.
     */
    @objc
    func availableFromDevice(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) {
        guard checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
            return
        }
        
        guard let connected = connections[deviceId] else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 203)
            reject("device_not_connected", "Device is not currently connected", error)
            return
        }
        
        resolve(connected.available())
    }
    
    func sendEvent(_ eventName: String, body: Any?) {
         guard let bridge = self.bridge else {
             NSLog("Error when sending event \(eventName) with body \(body ?? ""); Bridge not set")
             return
         }
         
         guard (listeners[eventName] != nil || listeners[eventName] == 0) else {
             NSLog("Sending '%@' with no listeners registered; was skipped", eventName)
             return
         }
         
         var data: [Any] = [eventName]
         if let actualBody = body {
             data.append(actualBody)
         }
         
         bridge.enqueueJSCall("RCTDeviceEventEmitter",
                              method: "emit",
                              args: data,
                              completion: nil)
     }
     
     @objc
     func addListener(
        _ requestedEvent: String
     ) {
        var eventName = requestedEvent
        var deviceId: String?
         
        if (requestedEvent.contains("@")) {
            let split = requestedEvent.split(separator: "@")
            eventName = String(split[0])
            deviceId = String(split[1])
        }
         
        guard EventType.allCases.firstIndex(where: { $0.name == eventName}) ?? -1 >= 0 else {
            NSLog("%@ is not a supported EventType", eventName)
            return
        }
         
        // When saving the listener, we need to use the requested event now that we know
        // it's legal, this way we maintain the DEVICE_READ@<serialNumber>
        let listenerCount = listeners[requestedEvent] ?? 0
        listeners[requestedEvent] = listenerCount + 1
         
        if let forDevice = deviceId {
            onAddListener(eventName, deviceId: forDevice)
        }
     }
     
    @objc
    func removeListener(_ requestedEvent: String) throws {
        var eventName = requestedEvent
        var eventDevice: String?
         
        if (requestedEvent.contains("@")) {
            let split = requestedEvent.split(separator: "@")
            eventName = String(split[0])
            eventDevice = String(split[1])
        }
         
        guard EventType.allCases.firstIndex(where: { $0.name == eventName}) ?? -1 >= 0 else {
            NSLog("%@ is not a supported EventType", eventName)
            return
        }
         
        let listenerCount = listeners[eventName] ?? 0
         
        if listenerCount > 0 {
            listeners[eventName] = listenerCount - 1
             
            if let deviceId = eventDevice {
                onRemoveListener(eventName, deviceId: deviceId)
            }
        }
    }
     
    @objc
    func removeAllListeners(_ requestedEvent: String) throws {
        var eventName = requestedEvent
        var eventDevice: String?
        
        if (requestedEvent.contains("@")) {
            let split = requestedEvent.split(separator: "@")
            eventName = String(split[0])
            eventDevice = String(split[1])
        }
         
        guard EventType.allCases.firstIndex(where: { $0.name == eventName}) ?? -1 >= 0 else {
            NSLog("%@ is not a supported EventType", eventName)
            return
        }
         
        let listenerCount = listeners[eventName] ?? 0
         
        if listenerCount > 0 {
            listeners[eventName] = listenerCount - 1
             
            if let deviceId = eventDevice {
                onRemoveListener(eventName, deviceId: deviceId)
            }
        }

    }
     
    func onAddListener(_ eventName: String, deviceId: String) {
        if var connection = connections[deviceId] {
            connection.dataReceivedDelegate = self;
        } else {
            NSLog("Device %@ is not currently connected, unable to set delegate", deviceId)
        }
    }
    
    func onRemoveListener(_ eventName: String, deviceId: String) {
        if var connection = connections[deviceId] {
            connection.dataReceivedDelegate = nil;
        } else {
            NSLog("Device %@ is not currently connected, unable to remove delegate", deviceId)
        }
    }
    
    func onRemoveAllListeners(_ eventName: String, deviceId: String) {
        if var connection = connections[deviceId] {
            connection.dataReceivedDelegate = nil;
        } else {
            NSLog("Device %@ is not currently connected, unable to remove delegate", deviceId)
        }
    }
}

// MARK: BluetoothReceivedDelegate implementation
/**
 * Extension implementing the DataReceivedDelegate
 *
 * Responsible for accepting data from the device (when a listener has been requested) and passing
 * such data through to React Native.
 */
extension RNBluetoothClassic : DataReceivedDelegate {
    
    /**
     * Receives data from the device, this data should already be:
     * - Encoded correctly
     * - Bundled correctly
     * - essentiatlly everything ready to go to React Native
     *
     * Once there, Javascript will be responsible for parsing the data.
     */
    func onReceivedData(fromDevice: EAAccessory, receivedData: String) {
        // Need to gaurd against whether to send this information.  But for now
        // we'll just send it anyhow.
        NSLog("(RNBluetoothClassic:onReceiveData) Sending DEVICE_READ with data: %@", receivedData)
        let bluetoothMessage:BluetoothMessage = BluetoothMessage<String>(
            fromDevice: NativeDevice(accessory: fromDevice), data: receivedData)
        sendEvent("\(EventType.DEVICE_READ.name)@\(fromDevice.serialNumber)", body: bluetoothMessage.map())
    }

}
