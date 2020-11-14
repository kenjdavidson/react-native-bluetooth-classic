//
//  DeviceConnectionFactory.swift
//  react-native-bluetooth-classic
//
//  Created by Ken Davidson on 2020-11-09.
//

import Foundation
import ExternalAccessory

/**
 * Provides a method for creating DeviceConnection(s).  The factories are used by the RNBluetoothClassic (module) during
 * the connection process.
 *
 * @author kendavidson
 */
protocol DeviceConnectionFactory {
    func create(accessory: EAAccessory, options: Dictionary<String,Any>) -> DeviceConnection
}
