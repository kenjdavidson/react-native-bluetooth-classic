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
    var session: EASession?;
    
    init(_ accessory: EAAccessory) {
        self.accessory = accessory
    }
    
    func write() -> NSDictionary {
        let dict: NSDictionary = NSMutableDictionary()
        dict.setValue(accessory.name, forKey: "name")
        dict.setValue(accessory.serialNumber, forKey: "address")
        dict.setValue(accessory.serialNumber, forKey: "id")
        
        // Extra IOS specific details
        dict.setValue(accessory.modelNumber, forKey: "modelNumber")
        dict.setValue(accessory.protocolStrings, forKey: "protocols")
        dict.setValue(accessory.isConnected, forKey: "connected")
        return dict;
    }
    
    func connect(
        protocolString: String,
        delegate: StreamDelegate
    ) {
        session = EASession(accessory: accessory, forProtocol: protocolString)
        
        if (session != nil) {
            session?.inputStream?.delegate = delegate
            session?.inputStream?.open()
            
            session?.outputStream?.delegate = delegate
            session?.outputStream?.open()            
        }
    }
    
    func disconnect() {
        session?.inputStream?.close()
        session?.outputStream?.close()        
        session = nil
    }
    
    func hasAvailableBytes() -> Bool {
        return session?.inputStream?.hasBytesAvailable ?? false
    }
}
