//
//  RNModuleInitializer.swift
//  BluetoothClassicExample
//
//  Created by Ken Davidson on 2020-01-13.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import RNBluetoothClassic

@objc(RNModuleInitializer)
final class RNModuleInitialiser: NSObject {
  override init() {
    super.init()
  }
}

extension RNModuleInitialiser: RCTBridgeDelegate {
    func sourceURL(for bridge: RCTBridge!) -> URL! {
      return nil
    }

    func extraModules(for bridge: RCTBridge!) -> [RCTBridgeModule]! {
      var extraModules = [RCTBridgeModule]()
      extraModules.append(RNBluetoothClassic("\n",String.Encoding.ascii))
      return extraModules
    }

}
