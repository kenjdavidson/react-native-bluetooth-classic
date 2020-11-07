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
    case READ = "read"
    case ERROR = "error"
    
    static func asDictionary() -> NSDictionary {
        let events:NSDictionary = NSMutableDictionary()
        
        for event in EventType.allCases {
            events.setValue(event.rawValue, forKey: "\(event)")
        }
        
        return events
    }
    
    static func asArray() -> [String] {
        var events:[String] = [String]()
        
        for event in EventType.allCases {
            events.append(event.rawValue)
        }
        
        return events;
    }
}
