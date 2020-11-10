//
//  BTEvent.swift
//  RNBluetoothClassic
//
//  Contains available Bluetooth Events used throughout the module.
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

enum EventType: String, CaseIterable {
    case BLUETOOTH_ENABLED = "bluetoothEnabled"
    case BLUETOOTH_DISABLED = "bluetoothDisabled"
    case DEVICE_CONNECTED = "deviceConnected"
    case DEVICE_DISCONNECTED = "deviceDisconnected"
    case DEVICE_READ = "read"
    case ERROR = "error"
    
    var name: String {
        let fullname = String(reflecting: self)
        let index = fullname.lastIndex(of: ".")!
        return String(fullname[fullname.index(after: index)...])
    }
}
