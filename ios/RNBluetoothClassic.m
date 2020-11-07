//
//  RNBluetoothClassic.m
//  RNBluetoothClassic
//
//  Created by Ken Davidson on 2019-06-17.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "React/RCTBridgeModule.h"
#import "React/RCTEventEmitter.h"

/**
 * Exports the RNBluetoothClassic module.
 */
@interface RCT_EXTERN_MODULE(RNBluetoothClassic, RCTEventEmitter)

/**
 * Determine whether the device currently has Bluetooth enabled.
 *
 * @param resolve
 * @param rejecter
 */
RCT_EXTERN_METHOD(isBluetoothEnabled: (RCTPromiseResolveBlock)resolver
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Retreives a list of currently bonded/paired device.  This will only return bonded devices which conform
 * to the set of MFi protocols provided in the Info.plist file.
 *
 * @param resolve
 * @param rejecter
 */
RCT_EXTERN_METHOD(getBondedDevices: (RCTPromiseResolveBlock)resolver
                  rejecter: (RCTPromiseRejectBlock)reject)                                 

/**
 * Request to connect to the specified device.
 *
 * @param deviceId the device Id/address to which we will try to connect
 * @param options connection parameters
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(connectToDevice: (NSString)deviceId
                  options: (ReadableMap)options
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Request disconnection from specified device.
 *
 * @param deviceId the device Id/Address from which we will disconnect
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(diconnectFromDevice: (NSString)deviceId
                  resolver: (RCTPromiseResolveBlock)resolver
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Determine whether the reqeusted device is connected.
 *
 * @param deviceId the device Id/Address to check
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(isDeviceConnected: (NSString)deviceId
                  resolver: (RCTPromiseResolveBlock)resolver
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Get the device if connected.
 *
 * @param deviceId the device Id/Address to check for connection
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(getConnectedDevice: (NSString)deviceId
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Attempt to write data to the device
 *
 * @param deviceId the device Id/Address to check for connection
 * @param message the message that will be written to the device
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(writeToDevice: (NSString)deviceId
                  message: (NSString)message
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Attempt to read from the device.  This will return the appropriate data based on the DeviceConnection
 * implementation.
 *
 * @param deviceId the device Id/Address to check for connection
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(readFromDevice: (NSString)deviceId
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Check whether there is data available on the device.  The response to this is solely based on the
 * DeviceConnection implementation; it could refer to the total number of bytes or the number of
 * full mssages.
 *
 * @param deviceId the device Id/Address to check for connection
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(available: (NSString)deviceId
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Attempts to clear any data currently in the device buffer.
 *
 * @param deviceId the device Id/Address to check for connection
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(clearFromDevice: (NSString)deviceId
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)                

@end
