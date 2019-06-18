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

enum BTEvent : String, CaseIterable {
    case BLUETOOTH_ENABLED = "bluetoothEnabled"
    case BLUETOOTH_DISABLED = "bluetoothDisabled"
    case BLUETOOTH_CONNECTED = "bluetoothConnected"
    case BLUETOOTH_DISCONNECTED = "bluetoothDisconnected"
    case CONNECTION_SUCCESS = "connectionSuccess"        // Promise only
    case CONNECTION_FAILED = "connectionFailed"          // Promise only
    case CONNECTION_LOST = "connectionLost"
    case READ = "read"
    case ERROR = "error"
    
    static func eventNamesDictionary() -> NSDictionary {
        let events:NSDictionary = NSDictionary()
        
        for event in BTEvent.allCases {
            events.setValue(event.self, forKey: event.rawValue)
        }
        
        return events
    }
    
    static func eventNamesArray() -> [String] {
        var events:[String] = [String]()
        
        for event in BTEvent.allCases {
            events.append(event.rawValue)
        }
        
        return events;
    }
}
