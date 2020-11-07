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
class RNBluetoothClassic : RCTEventEmitter {
    
    let eaManager: EAAccessoryManager
    let cbCentral: CBCentralManager
    let notificationCenter: NotificationCenter
    let supportedProtocols: [String]
    
    var peripherals: Dictionary<String,DeviceConnection>
    
    /**
     * Initializes the RNBluetoothClassic module.  At this point it's not quite as customizable as the
     * Java version, but I'm slowly working on figuring out how to incorporate the same logic in a
     * Swify way, but my ObjC and Swift is not strong, very very not strong.
     */
    override init() {
        super.init()
        
        self.eaManager = EAAccessoryManager.shared()
        self.cbCentral = CBCentralManager()
        self.notificationCenter = NotificationCenter.default
        self.supportedProtocols = Bundle.main
            .object(forInfoDictionaryKey: "UISupportedExternalAccessoryProtocols") as! [String]
        
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
     * to react native.  I think this has to do ony with Bonding and not Connecting in the way that we use the
     * terms within this library.  If this is the case, we'll need to decide if this needs to be changed to
     * a DEVICE_BONDED event and create the same on Android.
     */
    @objc
    func accessoryDidConnect(_ notification:Notification) {
        // Unlike the disconnect we just need to pass the event to the application.  It
        // will decide whether or not to connect.
        if let connected: EAAccessory = notification.userInfo!["EAAccessoryKey"] as? EAAccessory {
            sendEvent(withName: EventType.DEVICE_CONNECTED.rawValue,
                      body: NativeDevice(accessory: connected).map())
        }
    }
    
    /**
     * Received a disconnct notification from IOS.  If we are currently connected to this device, we need to disconnect it
     * and remove it from the connected peripherals map.
     */
    @objc
    func accessoryDidDisconnect(_ notification:Notification) {
        if let disconnected: EAAccessory = notification.userInfo!["EAAccessoryKey"] as? EAAccessory {
            // If we are currently connected to this, then we need to
            // disconnected it and remove the current peripheral
            if let currentDevice = peripherals.removeValue(forKey: disconnected.serialNumber) {
                currentDevice.disconnect()
            }
            
            // Finally send the notification
            sendEvent(withName: EventType.DEVICE_DISCONNECTED.rawValue,
                      body: NativeDevice(accessory: disconnected).map())
        }
    }
    
    /**
     RCTEventEmitter -
     Turned off the main queue setup for now - testing.  But this will need to be turned
     on as the ExternalAccessory event handling needs to occur on a separate thread and
     be haneled correctly.
     */
    override static func requiresMainQueueSetup() -> Bool {
        return true;
    }
    
    /**
     RCTEventEmitter -
     Return the constants for BTEvents and BTCharsets specific to IOS.
     */
    override func constantsToExport() -> [AnyHashable : Any]! {
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
        resolver resolve: RCTPromiseResolveBlock,
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
        let error = NSError(domain: "kjd.reactnative.bluetooth", code: 100)
        reject("bluetooth_disabled", "Bluetooth is not enabled", error)
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
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
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
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        guard peripherals[deviceId] != nil else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 200)
            reject("device_connected", "Device is already connected", error)
        }
        
        // Now check to see that the device is still connected and available
        // using the EAAccessoryManager, if found we create a new BluetoothDevice
        // which will be responsible for managing our connection
        if let accessory = eaManager.connectedAccessories.first(where: {
            $0.serialNumber == deviceId
        }) {
            if let protocolString:String = determineProtocolString(forDevice: accessory) {
                NSLog("(RNBluetoothClassic:connect) Connecting to %@ with %@", accessory.name, protocolString)
                let connection = DelimitedStringDeviceConnectionImpl(accessory: accessory, properties: options)
                connection.connect()
                
                peripherals[deviceId] = connection
                resolve(NativeDevice(accessory: accessory).map())
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
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        guard let connected = peripherals.removeValue(forKey: deviceId) else {
            let error = NSError(domain: "kjd.reactnative.bluetooth", code: 203)
            reject("device_not_connected", "Device is not currently connected", error)
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
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        NSLog("(RNBluetoothClassic:isConnected) isConnected %@", peripheral?.accessory.name ?? "nil")
        if let connected = peripheral {
            resolve(connected.accessory.isConnected)
        } else {
            resolve(false)
        }
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
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        NSLog("(RNBluetoothClassic:getConnectedDevice) Determine whether %@ is connected", peripheral?.accessory.name ?? "nil")
        if peripheral != nil {
            resolve(peripheral?.asDictionary())
        } else {
            reject("error", "No bluetooth device connected", nil)
        }
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
        _ message: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        NSLog("(RNBluetoothClassic:writeToDevice) Writing %@ to device %@", message, peripheral?.accessory.name ?? "nil")
        if let currentDevice = peripheral, let decoded = Data(base64Encoded: message) {
            currentDevice.writeToDevice(String(data: decoded, encoding: .utf8)!)
            resolve(true)
        } else {
            reject("error", "Not currently connected to a device", nil)
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
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        resolve(peripheral?.readFromDevice() ?? "")
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
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        if let currentDevice = peripheral {
            currentDevice.clear()
        }
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
        guard !checkBluetoothAdapter() else {
            rejectBluetoothDisabled(rejecter: reject)
        }
        
        guard let p = peripheral else {
            let msg: String = "There is no currently connected devices from which to read data"
            reject("error", msg, nil)
            return
        }
        
        resolve(p.bytesAvailable())
    }

}

// MARK: BluetoothReceivedDelegate implementation

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
        NSLog("(RNBluetoothClassic:onReceiveData) Sending READ with data: %@", receivedData)
        let bluetoothMessage:BluetoothMessage = BluetoothMessage<String>(fromDevice: <#<<error type>>#>)
        sendEvent(withName: EventType.READ.rawValue, body: bluetoothMessage.asDictionary())
        
    }
}
