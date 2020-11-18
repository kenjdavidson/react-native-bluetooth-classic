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
 * Exports the RNBluetoothClassic native module to the RCTBridge.  RCT_EXTERN_MODULE is required
 * due to the project being developed in Swift.  I'm debating re-writing this module in Objective C, as there
 * are definite pros (from the time I started this) in not attempting to bridge the language gap.
 *
 * @author kendavidson
 */
@interface RCT_EXTERN_MODULE(RNBluetoothClassic, NSObject)

/**
 * Determine whether bluetooth is enabled on the device.  This is based on the Bluetooth manager
 * state (also on the version of IOS on which the app is being run).  This should always resolve
 * true|false and never be rejected
 *
 * @param resolver resolves the promise with true|false
 * @param rejecter rejects the promise - should never occur
 */
RCT_EXTERN_METHOD(isBluetoothEnabled: (RCTPromiseResolveBlock)resolver
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Retreives a list of bonded devices.  Note that in IOS External Accessor (MFi) devices are always
 * "connected", this is where a lot of confusion comes into play when working between Android and
 * IOS and this library.  Moving forward the term "bonded" means that the device is aware of the
 * peripheral while "connected" means there are open Sockets/Streams.
 *
 * @param resolver resolves promise with a list of bonded devices
 * @param rejecter rejects promise if the bluetooth is not currently enabled
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
RCT_EXTERN_METHOD(connectToDevice: (NSString *)deviceId
                  options: (NSDictionary)options
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Request disconnection from specified device.
 *
 * @param deviceId the device Id/Address from which we will disconnect
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(disconnectFromDevice: (NSString *)deviceId
                  resolver: (RCTPromiseResolveBlock)resolver
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Determine whether the reqeusted device is connected.
 *
 * @param deviceId the device Id/Address to check
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(isDeviceConnected: (NSString *)deviceId
                  resolver: (RCTPromiseResolveBlock)resolver
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Get the device if connected.
 *
 * @param deviceId the device Id/Address to check for connection
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(getConnectedDevice: (NSString *)deviceId
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
RCT_EXTERN_METHOD(writeToDevice: (NSString *)deviceId
                  withMessage: (NSString *)message
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
RCT_EXTERN_METHOD(readFromDevice: (NSString *)deviceId
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
RCT_EXTERN_METHOD(available: (NSString *)deviceId
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

/**
 * Attempts to clear any data currently in the device buffer.
 *
 * @param deviceId the device Id/Address to check for connection
 * @param resolver
 * @param rejecter
 */
RCT_EXTERN_METHOD(clearFromDevice: (NSString *)deviceId
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)                

/**
 * Adds a listener to the module.  Listeners can be generic: connect, disconnect, etc. or they can
 * be device specific read@AA:BB:CC:DD:EE.  Adding a listener will increment a counter
 * keyed on the provided eventType
 */
RCT_EXTERN_METHOD(addListener: (NSString *)requestedEvent)

/**
 * Remove a specified listener.  LIke addListener this can be generic or device specific
 */
RCT_EXTERN_METHOD(removeListener: (NSString *)requestedEvent)

/**
 * Removes all listeners of this type.  This sets the counter to 0 (stopping all messages from
 * being sent to React Native).
 */
RCT_EXTERN_METHOD(removeAllListeners: (NSString *)requestedEvent)

@end
