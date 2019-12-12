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
class RNBluetoothClassic : RCTEventEmitter, BluetoothRecievedDelegate {
    
    let eaManager: EAAccessoryManager
    let cbCentral: CBCentralManager
    let notificationCenter: NotificationCenter
    let supportedProtocols: [String]
    
    var peripheral:BluetoothDevice?
    var delimiter:String
    
    /**
     Initialize the RNBluetoothClassic.
     */
    override init() {
        self.eaManager = EAAccessoryManager.shared()
        self.cbCentral = CBCentralManager()
        self.notificationCenter = NotificationCenter.default
        self.supportedProtocols = Bundle.main
            .object(forInfoDictionaryKey: "UISupportedExternalAccessoryProtocols") as! [String]
        self.delimiter = "\n"
        
        super.init()
        
        registerForLocalNotifications()
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
        notificationCenter.addObserver(self, selector: #selector(accessoryDidConnect), name: .EAAccessoryDidConnect, object: nil)
        notificationCenter.addObserver(self, selector: #selector(accessoryDidDisconnect), name: .EAAccessoryDidDisconnect, object: nil)
    }
    
    @objc
    func accessoryDidConnect(_ notification:Notification) {
        // Unlike the disconnect we just need to pass the event to the application.  It
        // will decide whether or not to connect.
        if let connected: EAAccessory = notification.userInfo!["EAAccessoryKey"] as? EAAccessory {
            sendEvent(withName: BTEvent.BLUETOOTH_CONNECTED.rawValue, body: BluetoothDevice(connected).asDictionary())
        }
    }
    
    @objc
    override func startObserving() {
        NSLog("Starting to observe with listeners")
    }
    
    @objc
    override func stopObserving() {
        NSLog("Starting to observe with listeners")
    }
    
    @objc
    func accessoryDidDisconnect(_ notification:Notification) {
        if let disconnected: EAAccessory = notification.userInfo!["EAAccessoryKey"] as? EAAccessory {
            if let currentDevice = peripheral {
                if currentDevice.accessory.serialNumber == disconnected.serialNumber {
                    currentDevice.disconnect()
                    peripheral = nil
                }
            }
            
            sendEvent(withName: BTEvent.BLUETOOTH_DISCONNECTED.rawValue, body: notification.object)
        }
    }
    
    /**
     Remove all Observers and unregister with the NotificationCenter
     */
    private func unregisterForLocalNotifications() {
        notificationCenter.removeObserver(self)
        eaManager.unregisterForLocalNotifications()
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
     Match the getConstants() method on Android.  The difference being that
     in index.js we need to pull from different locations RNBluetoothClassic.BTEvents
     rather than RNBluetoothClassic.getConstants().BTEvents.
     */
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
            "BTEvents": BTEvent.asDictionary()
        ];
    }
    
    /**
     RCTEventEmitter -
     Return the supported events for the RCTEventEmitter to test.
     */
    override func supportedEvents() -> [String] {
        return BTEvent.asArray()
    }
    
    /**
     BluetoothReceivedDelegate -
     Loop through the received data looking for all instances of the provided delimiter and firing an
     onRead event for each one.
     */
    func onReceivedData(fromDevice: BluetoothDevice, receivedData: Data) -> Data {
        if let data = String(data: receivedData, encoding: .utf8) {
            let indexes = data.indexes(of: delimiter)
            var startIndex = data.startIndex
            
            for index in indexes {
                let message = String(data[startIndex..<index])
                
                NSLog("(RNBluetoothClassic:onReceiveData) Sending READ with data: %@", message)
                let bluetoothMessage:BluetoothMessage = BluetoothMessage<String>(fromDevice: fromDevice, data: message)
                sendEvent(withName: BTEvent.READ.rawValue, body: bluetoothMessage.asDictionary())
                
                startIndex = data.index(after: index)
            }
            
            return data[startIndex...].data(using: .utf8) ?? Data()
        }
        
        return receivedData
    }
    
    /**
     Attempts to request that bluetooth be enabled.  I don't believe that EAExternalAccessory
     allows for this, but I think that CBCentralManager does, I'll have to look into
     implementing this in the future.
     - parameter _: bluetooth was enabled successfully
     - parameter reject: unable to attempt/enable bluetooth
     */
    @objc
    func requestEnable(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        let msg: String = "requestEnabled is not implemented on IOS"
        reject("error", msg, nil)
    }
    
    /**
     Whether or not bluetooth is currently enabled - currently this is done by using the
     CoreBluetooth (BLE) framework, as it should hopefully be good enough for performing
     bluetooth system tasks.
     - parameter _: resovles with true|false based on enable
     - parameter reject: should never be rejected
     */
    @objc
    func isEnabled(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        if #available(iOS 10.0, *) {
            resolve(cbCentral.state == CBManagerState.poweredOn)
        } else {
            resolve(cbCentral.state.rawValue == CBCentralManagerState.poweredOn.rawValue)
        }
    }
    
    /**
     Lists currently connected/bonded devices - devices must have matching protocols
     to those configured in the .plist UISupportedExternalAccessoryProtocols key.
     The call should never be rejected, only resolved with an empty list.  Uses
     the EAAccessoryManager.shared().connectedAccessories to get the devices.
     - parameter _: resovles with the list (possibly empty)
     - parameter reject: should never be rejected
     */
    @objc
    func list(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        var accessories:[NSDictionary] = [NSDictionary]()
        for accessory in eaManager.connectedAccessories {
            let device = BluetoothDevice(accessory)
            accessories.append(device.asDictionary())
        }
        resolve(accessories)
    }
    
    /**
     Allows the user to pair a device with the phone.  Uses the
     EAAccessoryManager.shared().showBluetoothAccessoryPicker(withNameFilter, completion)
     which pops up a system dialog.  Currently this method just rejects automatically as
     the EAExternalAccessory library requires that pairing be done at the system
     level.
     - parameter _: the name predicate used for filtering, passed through
     - parameter resolve: saved for use with the completion delegate
     - parameter reject: saved for use with the completion delgate
     */
    @objc
    func discoverDevices(
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        let msg: String = "discoverDevices is not implemented on IOS"
        reject("error", msg, nil)
    }
    
    /**
     Unsure if this is possible, may be auto reject.  Currently this method just rejects
     automatically as the EAExternalAccessory library requires that discovery be done at
     the system level.
     - parameter _: resolve the cancellation
     - parameter reject: reject the cancellation
     */
    @objc
    func cancelDiscovery(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        let msg: String = "cancelDiscovery is not implemented on IOS"
        reject("error", msg, nil)
    }
    
    /**
     Attempts to pair a specific device - will get a list of available devices
     and then pair it as such.  Currently this method just rejects automatically as
     the EAExternalAccessory library requires that pairing be done at the system
     level.
     - parameter deviceId: the Address/Id of the device
     - parameter resolve: saved for use with the completion delegate
     - parameter reject: saved for use with the completion delgate
     */
    @objc
    func pairDevice(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        let msg: String = "pairDevice is not implemented on IOS"
        reject("error", msg, nil)
    }
    
    /**
     Unpair the specified device.  Currently this method just rejects automatically as
     the EAExternalAccessory library requires that pairing be done at the system
     level
     - parameter deviceId: the Address/Id of the device
     - parameter resolve: resolve the unpairing
     - parameter reject: reject the unpairing
     */
    @objc
    func unpairDevice(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        let msg: String = "unpairDevice is not implemented on IOS"
        reject("error", msg, nil)
    }
    
    /**
     Determines whether the device is still connected and attempts to open
     a connection to it.  This is done by providing the self to the BluetoothDevice
     as a delegate during the open request.  If the device cannot be connected to,
     fails connection or bluetooth is just not enabled, then the request
     is rejected.
     - parameter _: the device in which to connect
     - parameter resolve: resolve when the connection has been established
     - parameter reject: reject a failed connection
     */
    @objc
    func connect(
        _ deviceId: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        // First disconnect the current device if there was one selected
        if let toDisconnect = peripheral {
            toDisconnect.disconnect()
            peripheral = nil
        }
        
        // Now check to see that the device is still connected and available
        // using the EAAccessoryManager, if found we create a new BluetoothDevice
        // which will be responsible for managing our connection
        for accessory in eaManager.connectedAccessories {
            if accessory.serialNumber == deviceId {
                peripheral = BluetoothDevice(accessory)
                peripheral!.receivedDelegate = self
                break
            }
        }
        
        if let toConnect:BluetoothDevice = peripheral {
            // Determine the protocol to use, this is done by getting a list of the available
            // protocols from plist and selecting the first one matching the device.
            if let protocolString:String = determineProtocolString(forDevice: toConnect) {
                NSLog("(RNBluetoothClassic:connect) Connecting to %@ with %@", toConnect.accessory.name, protocolString)
                toConnect.connect(protocolString: protocolString)
                resolve(peripheral?.asDictionary())
            }
        } else {
            reject("error", "Unable to connect to device", nil)
        }
    }
    
    private func determineProtocolString(forDevice device:BluetoothDevice) -> String? {
        for supported in supportedProtocols {
            if (device.accessory.protocolStrings.contains(supported)) {
                return supported
            }
        }
        return nil
    }
    
    /**
     Disconnect from the currently connected device.
     - parameter _: resolve the disconnection
     - parameter reject: reject if the disconnection fails
     */
    @objc
    func disconnect(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        if let toDisconnect = peripheral {
            NSLog("(RNBluetoothClassic:disconnect) Disconnecting %@", toDisconnect.accessory.name)
            toDisconnect.disconnect()
            peripheral = nil
        }
        resolve(true)
    }
    
    /**
     Determine whether there is a connected device
     - parameter _: resolve the connection
     */
    @objc
    func isConnected(_ resolve: RCTPromiseResolveBlock,
                     rejecter reject: RCTPromiseRejectBlock) -> Void {
        NSLog("(RNBluetoothClassic:isConnected) isConnected %@", peripheral?.accessory.name ?? "nil")
        if let connected = peripheral {
            resolve(connected.accessory.isConnected)
        } else {
            resolve(false)
        }
    }
    
    /**
     Resolve the connected device.
     - parameter _: resolve with either the connected device or null.
     */
    @objc
    func getConnectedDevice(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock) -> Void {
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
     - parameter _: the Base64 encoded message
     - parameter resolve: resolve the message once sent
     */
    @objc
    func writeToDevice(
        _ message: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
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
     - parameter _: resolve with the available data
     */
    @objc
    func readFromDevice(_ resolve: RCTPromiseResolveBlock) -> Void {
        resolve(peripheral?.readFromDevice())
    }
    
    /**
     Attempts to read the buffer any/all data up to the delimiter.  If the delimiter is
     not found then then this simulates readFromDevice.
     - parameter until: the delimiter in which to read up to
     - parameter resolve: resolve with the available data
     */
    @objc
    func readUntilDelmiter(
        _ delimiter: String,
        resolver resolve: RCTPromiseResolveBlock
    ) -> Void {
        resolve(readUntil(delimiter))
    }
    
    /**
     Sets a new delimiter used for default reading
     - parameter _: the delimiter
     - parameter resolver: resolves the set delmiter
     */
    @objc
    func setDelimiter(
        _ delimiter: String,
        resolver resolve: RCTPromiseResolveBlock
    ) -> Void {
        self.delimiter = delimiter
        resolve(true)
    }
    
    /**
     Clear the buffer.
     - parameter _: resolves when clear is complete
     */
    @objc
    func clear(_ resolve: RCTPromiseResolveBlock) {
        if let currentDevice = peripheral {
            currentDevice.clear()
        }
        resolve(true)
    }
    
    /**
     Resolves with the amount of data available to be read.  This is the total
     buffer length, with no regard for the delimiter.  Should possibly add in
     a delimiter value.
     - parameter _: resolve with the availabel data size
     */
    @objc
    func isAvailable(_ resolve: RCTPromiseResolveBlock) {
        resolve(peripheral?.hasBytesAvailable() ?? false)
    }
    
    /**
     Attempts to set the adapter name - this is a hold over from the Android
     module and should just resolve or reject.
     */
    @objc
    func setAdapterName(
        _ name: String,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        let msg: String = "setAdapterName is not implemented on IOS"
        reject("error", msg, nil)
    }
    
    /**
     Responsible for reading from the buffer until the delimiter is found (or all
     data).  Clears the data which was read, to allow for other reads.
     - parameter _: the delimiter to which we will read
     - returns: the read data String or nil
     */
    private func readUntil(_ delimiter: String) -> String? {
        if let currentDevice = peripheral {
            return currentDevice.readFromDevice(withDelimiter: delimiter)
        }
        
        return nil
    }
    
}

