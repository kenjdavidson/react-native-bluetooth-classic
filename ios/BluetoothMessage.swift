//
//  BluetoothMessage.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-20.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

class BluetoothMessage<T> : NSObject {
    
    public private(set) var data:T
    public private(set) var timestamp:Date
    
    init(_ data:T) {
        self.data = data
        self.timestamp = Date()
    }
    
    func asDictionary() -> Dictionary<String,Any> {
        return [
            "data": data,
            "timestamp": timestamp
        ]
    }
}
