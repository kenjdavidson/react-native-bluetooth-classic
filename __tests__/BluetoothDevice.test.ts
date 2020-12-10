import { NativeEventEmitter, Platform } from "react-native";
import BluetoothNativeDevice from "../src/BluetoothNativeDevice";
import BluetoothModule from "../src/BluetoothModule";
import BluetoothDevice from "../src/BluetoothDevice";

import Jest from "jest";
import { createMock } from "ts-auto-mock";
import { On, method } from "ts-auto-mock/extension";
import { StandardOptions } from "../src/BluetoothNativeModule";

describe("Bluetooth Device", () => {
  let nativeDevice: BluetoothNativeDevice;
  let nativeModule: BluetoothModule;

  let device: BluetoothDevice;

  beforeEach(() => {
    nativeDevice = createMock<BluetoothNativeDevice>();
    nativeModule = createMock<BluetoothModule>();

    device = new BluetoothDevice(nativeDevice, nativeModule);
  });

  test("BluetoothDevice fields should match BluetoothNativeDevice fields", () => {
    expect(device.name).toBe(nativeDevice.name);
    expect(device.address).toBe(nativeDevice.address);
    expect(device.id).toBe(nativeDevice.id);
    expect(device.extra).toBe(nativeDevice.extra);
    expect(device.rssi).toBe(nativeDevice.rssi);
    expect(device.deviceClass).toBe(nativeDevice.deviceClass);
    expect(device.bonded).toBe(nativeDevice.bonded);
  });

  describe("Bluetooth Device isConnected", () => {
    test("Bluetooth Module should be called with Device address", () => {
      
    })
  });

  describe("Bluetooth Device connect", () => {
    test("Connect accepts no options", () => {
      device.connect();      
    });

    test("Connect accepts standard options", () => {
      device.connect({delimiter: "\n"});
    });

    test("Connect accepts extended options", () => {
      interface MyOptions extends StandardOptions {
        someRandomOption?: string
      }

      device.connect<MyOptions>({someRandomOption: "random"});
    });
  });
});
