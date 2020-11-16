
import { mocked } from 'ts-jest/utils'
import { Platform } from 'react-native';
import BluetoothNativeModule from '../lib/BluetoothNativeModule';
import BluetoothModule from '../src/BluetoothModule';

jest.mock('react-native', () => ({
  Platform: { OS: 'ios' }
}));

describe("React Native Platform", () => {
  test("Platform.OS should be 'ios'", () => {
    expect(Platform.OS).toBe('ios');
  });
});

// let nativeModule: BluetoothNativeModule = mocked(<BluetoothNativeModule>{}, true);
// let bluetoothModule: BluetoothModule;

// describe("BluetoothModule Android Only features", () => {  
//   test("startDiscovery should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });

//   test("cancelDiscovery should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });

//   test("pairDevice should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });

//   test("unpairDevice should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });

//   test("accept should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });

//   test("cancelAccept should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });

//   test("requestBluetoothEnabled should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });

//   test("setBluetoothAdapterName should throw", () => {
//     expect(bluetoothModule.startDiscovery()).toThrow();
//   });
// });
