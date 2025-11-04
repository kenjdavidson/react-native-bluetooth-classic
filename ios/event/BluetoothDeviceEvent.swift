//
//  BluetoothDeviceEvent.swift
//  react-native-bluetooth-classic
//
//  Wraps device event data within an event containing the device and timestamp.
//  This is used for device connection/disconnection/discovery events.
//

import Foundation

class BluetoothDeviceEvent: NSObject, Mappable {
    public private(set) var device: NativeDevice
    public private(set) var timestamp: Date
    public private(set) var eventType: EventType
    
    init(eventType: EventType, device: NativeDevice) {
        self.eventType = eventType
        self.device = device
        self.timestamp = Date()
    }
    
    func map() -> NSDictionary {
        return [
            "eventType": eventType.name,
            "device": device.map(),
            "timestamp": ISO8601DateFormatter().string(from: timestamp)
        ]
    }
}
