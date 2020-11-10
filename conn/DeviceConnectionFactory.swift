//
//  DeviceConnectionFactory.swift
//  react-native-bluetooth-classic
//
//  Created by Ken Davidson on 2020-11-09.
//

import Foundation
import ExternalAccessory

protocol DeviceConnectionFactory {
    func create(accessory: EAAccessory, options: Dictionary<String,Any>) -> DeviceConnection
}
