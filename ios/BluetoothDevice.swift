//
//  BluetoothDevice.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import ExternalAccessory

class BluetoothDevice: NSObject {
    
    let accessory: EAAccessory;
    
    init(_ accessory: EAAccessory) {
        self.accessory = accessory
    }
    
    func write() -> NSDictionary {
        let dict: NSDictionary = NSDictionary()
        dict.setValue(accessory.name, forKey: "name")
        dict.setValue(accessory.serialNumber, forKey: "address")
        dict.setValue(accessory.name, forKey: "id")
        
        // Extra IOS specific details
        dict.setValue(accessory.modelNumber, forKey: "modelNumber")
        dict.setValue(accessory.protocolStrings, forKey: "protocols")
        return dict;
    }
}
