import {BluetoothEventType} from "../src/BluetoothEvent";

describe("Available Bluetooth Event Types", () => {
  test("Bluetooth Enabled", () => {
    expect(BluetoothEventType.BLUETOOTH_ENABLED).toBe("BLUETOOTH_ENABLED");
  });

  test("Bluetooth Disabled", () => {
    expect(BluetoothEventType.BLUETOOTH_DISABLED).toBe("BLUETOOTH_DISABLED");
  });

  test("Device Connected", () => {
    expect(BluetoothEventType.DEVICE_CONNECTED).toBe("DEVICE_CONNECTED");
  });

  test("Device Disconnected", () => {
    expect(BluetoothEventType.DEVICE_DISCONNECTED).toBe("DEVICE_DISCONNECTED");
  });

  test("Device Read", () => {
    expect(BluetoothEventType.DEVICE_READ).toBe("DEVICE_READ");
  });

  test("Bluetooth Error", () => {
    expect(BluetoothEventType.ERROR).toBe("ERROR");
  });
});