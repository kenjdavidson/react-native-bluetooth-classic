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

@objc(RNBluetoothClassic)
class RNBluetoothClassic : RCTEventEmitter, StreamDelegate {
    
    let manager: EAAccessoryManager
    let central: CBCentralManager
    var peripheral:BluetoothDevice?;
    
    /**
     Initialize the RNBluetoothClassic.
     */
    override init() {
        self.manager = EAAccessoryManager.shared()
        self.central = CBCentralManager()
        
        super.init()
    }
    
    
    /**
     RCTEventEmitter -
     Turned off the main queue setup for now - testing.  But this will need to be turned
     on as the ExternalAccessory event handling needs to occur on a separate thread and
     be haneled correctly.
     */
    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return false;
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
     StreamDelegate -
     handles in/out stream events
     */
    func stream(
        _ aStream: Stream,
        handle eventCode: Stream.Event
    ) {
        NSLog("Stream data available")
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
        reject("error", msg, NSError())
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
            resolve(central.state == CBManagerState.poweredOn)
        } else {
            resolve(central.state.rawValue == CBCentralManagerState.poweredOn.rawValue)
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
        for accessory in manager.connectedAccessories {
            let device = BluetoothDevice(accessory)
            accessories.append(device.write())
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
    func discoverUnpairedDevices(
        _ predicate: NSPredicate?,
        resolver resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
        let msg: String = "discoverUnpairedDevices is not implemented on IOS"
        reject("error", msg, NSError())
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
        reject("error", msg, NSError())
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
        reject("error", msg, NSError())
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
        reject("error", msg, NSError())
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
        if peripheral != nil {
            peripheral?.disconnect()
            peripheral = nil
        }
        
        // Now check to see that the device is still connected and available
        // using the EAAccessoryManager, if found we create a new BluetoothDevice
        // which will be responsible for managing our connection
        let accessories = manager.connectedAccessories
        for accessory in accessories {
            if accessory.name == deviceId {
                peripheral = BluetoothDevice(accessory)
                break
            }
        }
        
        if peripheral != nil {
            // Determine the protocol to use, this is done by getting a list of the available
            // protocols from plist and selecting the first one matching the device.
            let protocolString: String? = nil
            peripheral?.connect(protocolString: protocolString!, delegate: self)
            resolve(peripheral?.write())
        }
        
        reject("error", "Unable to connect to device ${deviceId}", NSError())
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
        if peripheral != nil {
            peripheral?.disconnect()
            peripheral = nil
            resolve(true)
        }
        
        resolve(false)
    }
    
    /**
     Determine whether there is a connected device
     - parameter _: resolve the connection
     */
    @objc
    func isConnected(_ resolve: RCTPromiseResolveBlock) -> Void {
        resolve(peripheral?.accessory.isConnected ?? false)
    }
    
    /**
     Resolve the connected device.
     - parameter _: resolve with either the connected device or null.
     */
    @objc
    func getConnectedDevice(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock) -> Void {
        if peripheral != nil {
            resolve(peripheral?.write())
        }
        reject("error", "No bluetooth device connected", NSError())
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
        let msg: String = "writeToDevice is not implemented on IOS"
        reject("error", msg, NSError())
    }
    
    /**
     Attempts to read all of the data from the buffer, ignoring the delimiter.  If no
     data is in the buffer, an empty String will be returned.
     - parameter _: resolve with the available data
     */
    @objc
    func readFromDevice(_ resolve: RCTPromiseResolveBlock) -> Void {
        
    }
    
    /**
     Attempts to read the buffer any/all data up to the delimiter.  If the delimiter is
     not found then then this simulates readFromDevice.
     - parameter _: the delimiter in which to read up to
     - parameter resolve: resolve with the available data
     */
    @objc
    func readUntilDelimiter(
        _ delimiter: String,
        resolver resolve: RCTPromiseResolveBlock
        ) -> Void {
        
    }
    
    /**
     Attempts to read the buffer any/all data up until the default delimiter.
     - parameter resolve: resolve with the available data
     */
    @objc
    func readUntilDelimiter(_ resolve: RCTPromiseResolveBlock) -> Void {
    
    }
    
    /**
     Sets a new delimiter used for default reading
     - parameter _: the delimiter
     - parameter resolver: resolves the set delmiter
     */
    @objc
    func setDelimiter(
        _ delimter: String,
        resolver resolve: RCTPromiseResolveBlock
        ) -> Void {
        
    }
    
    /**
     Clear the buffer.
     - parameter _: resolves when clear is complete
     */
    @objc
    func clear(_ resolve: RCTPromiseResolveBlock) {
        
    }
    
    /**
     Resolves with the amount of data available to be read.  This is the total
     buffer length, with no regard for the delimiter.  Should possibly add in
     a delimiter value.
     - parameter _: resolve with the availabel data size
     */
    @objc
    func isAvailable(_ resolve: RCTPromiseResolveBlock) {
        resolve(peripheral?.hasAvailableBytes())
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
        
    }
    
    /**
     Responsible for reading from the buffer until the delimiter is found (or all
     data).  Clears the data which was read, to allow for other reads.
     - parameter _: the delimiter to which we will read
     - returns: the read data String or nil
     */
    private func readUntil(_ delimiter: String) -> String? {
        return nil;
    }
    
}

