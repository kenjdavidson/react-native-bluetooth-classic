//
//  DeviceConnection.swift
//  react-native-bluetooth-classic
//
//  Created by Ken Davidson on 2020-11-06.
//

import Foundation
import ExternalAccessory

/**
 * Provides handling of received data when requesting that new data/messages be provided automatically.
 *
 * @author kendavidson
 */
protocol DataReceivedDelegate {
    
    /**
     * Data was received from the specified device and has been decoded appropriately by the
     * connection.
     */
    func onReceivedData(fromDevice: EAAccessory, receivedData: String)
    
}

/**
 * Provides the required protocol for communication between the RNBluetoothClassic (module) and the EAAccessory.  This
 * is a little more interactive on Android, where different types of connectors, acceptors and connections are available for
 * interacting with a BluetoothSocket.
 *
 * @author kendavidson
 */
protocol DeviceConnection {
    
    /**
     * Provides a method for the RNBluetoothClassic (module) to receive incoming data from the EAAccessory.
     * Also allows the listening to be turned on/off as required.
     */
    var dataReceivedDelegate: DataReceivedDelegate? { get set }
    
    /**
     * The accessory to which this connection references
     */
    var accessory: EAAccessory { get }
    
    /**
     * Attempt to connect to the connections EAAccessory using the properties provided
     * using initialization.
     */
    func connect() throws
    
    /**
     * Disconnect from the device
     */
    func disconnect()
    
    /**
     * Get the number of bytes, messages, etc (based on implementation) available for reading
     * on the connections buffer.
     */
    func available() -> Int
    
    /**
     * Write data to the device
     */
    func write(_ data:Data) -> Bool
    
    /**
     * Read from the device buffer.  The amount of data read should match what would
     * have been counted from available.
     */
    func read() -> String?
    
    /**
     * Clear the device
     */
    func clear()
    
}
