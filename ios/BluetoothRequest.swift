//
//  BTRequest.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

enum BluetoothRequest: Int, CaseIterable {
    case ENABLE_BLUETOOTH = 1, PAIR_DEVICE
    
    var name: String {
        let fullname = String(reflecting: self)
        let index = fullname.lastIndex(of: ".")!
        return String(fullname[fullname.index(after: index)...])
    }
}
