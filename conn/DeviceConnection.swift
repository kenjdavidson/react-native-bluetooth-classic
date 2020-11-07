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
 */
protocol DataReceivedDelegate {
    
    /**
     * Data was received from the specified device and has been decoded appropriately by the
     * connection.
     */
    func onReceivedData(fromDevice: EAAccessory, receivedData: String)
    
}

/**
 *
 */
protocol DeviceConnection {
    
    /**
     * The delegate which will receive new data messages if available
     */
    var dataReceivedDelegate: DataReceivedDelegate? { get set }
    
    /**
     * The accessory to which this connection references
     */
    var accessory: EAAccessory: { get }
    
    /**
     * Attempt to connect to the connections EAAccessory using the properties provided
     * using initialization.
     */
    func connect()
    
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
    func write(_ message:String)
    
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
