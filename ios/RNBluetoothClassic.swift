//
//  RNBluetoothClassic.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import ExternalAccessory

@objc(RNBluetoothClassic)
class RNBluetoothClassic : RCTEventEmitter {
    
    let manager: EAAccessoryManager
    
    /**
     Initialize the RNBluetoothClassic.
     */
    override init() {
        self.manager = EAAccessoryManager.shared()
        
        super.init()
    }
    
    /**
     Match the getConstants() method on Android.  The difference being that
     in index.js we need to pull from different locations RNBluetoothClassic.BTEvents
     rather than RNBluetoothClassic.getConstants().BTEvents.
     */
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
            "BTEvents": BTEvent.eventNamesDictionary()
        ];
    }
    
    /**
     Return the supported events for the RCTEventEmitter to test.
     */
    override func supportedEvents() -> [String] {
        return BTEvent.eventNamesArray()
    }
    
    /**
     Turned off the main queue setup for now - testing.  But this will need to be turned
     on as the ExternalAccessory event handling needs to occur on a separate thread and
     be haneled correctly.
     */
    func requiresMainQueueSetup() -> Bool {
        return false;
    }
    
    /**
     Attempts to request that bluetooth be enabled.
     - parameter _: bluetooth was enabled successfully
     - parameter reject: unable to attempt/enable bluetooth
     */
    @objc
    func requestEnable(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
        ) -> Void {
        
    }
    
    /**
     Whether or not bluetooth is currently enabled.
     - parameter _: resovles with true|false based on enable
     - parameter reject: should never be rejected
     */
    @objc
    func isEnabled(_ resolve: RCTPromiseResolveBlock) -> Void {
        
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
    func list(_ resolve: RCTPromiseResolveBlock) -> Void {        
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
     which pops up a system dialog.
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
    
    }
    
    /**
     Unsure if this is possible, may be auto reject
     - parameter _: resolve the cancellation
     - parameter reject: reject the cancellation
     */
    @objc
    func cancelDiscovery(
        _ resolve: RCTPromiseResolveBlock,
        rejecter reject: RCTPromiseRejectBlock
        ) -> Void {
        
    }
    
    /**
     Attempts to pair a specific device - will get a list of available devices
     and then pair it as such.
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
        
    }
    
    /**
     Unpair the specified device.
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
        
    }
    
    /**
     Opens an EASession to the requested device.
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
        
    }
    
    /**
     Determine whether there is a connected device
     - parameter _: resolve the connection
     */
    @objc
    func isConnected(_ resolve: RCTPromiseResolveBlock) -> Void {
        
    }
    
    /**
     Resolve the connected device.
     - parameter _: resolve with either the connected device or null.
     */
    @objc
    func getConnectedDevice(_ resolve: RCTPromiseResolveBlock) -> Void {
        
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
        resolver resolve: RCTPromiseResolveBlock
        ) -> Void {
        
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

