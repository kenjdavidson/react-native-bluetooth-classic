//
//  BTState.swift
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

enum BTState: Int {
    case DISCONNECTED = 0, CONNECTING, CONNECTED, DISCONNECTING;
}
