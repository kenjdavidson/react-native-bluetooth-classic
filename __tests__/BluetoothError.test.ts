import BluetoothError from "../src/BluetoothError";

describe("Available Bluetooth Errors", () => {
  test("Bluetooth Not Accepting", () => {
    expect(BluetoothError.BLUETOOTH_NOT_ACCEPTING).toBe("BLUETOOTH_NOT_ACCEPTING");
  });

  test("Bluetooth In Accepting", () => {
    expect(BluetoothError.BLUETOOTH_IN_ACCEPTING).toBe("BLUETOOTH_IN_ACCEPTING");
  });

  test("Bluetooth In Discovery", () => {
    expect(BluetoothError.BLUETOOTH_IN_DISCOVERY).toBe("BLUETOOTH_IN_DISCOVERY");
  });

  test("Already Connected", () => {
    expect(BluetoothError.ALREADY_CONNECTED).toBe("ALREADY_CONNECTED");
  });

  test("Already Connecting", () => {
    expect(BluetoothError.ALREADY_CONNECTING).toBe("ALREADY_CONNECTING");
  });

  test("Not Currently connected", () => {
    expect(BluetoothError.NOT_CURRENTLY_CONNECTED).toBe("NOT_CURRENTLY_CONNECTED");
  });

  test("BONDING_UNAVAILABLE_API", () => {
    expect(BluetoothError.BONDING_UNAVAILABLE_API).toBe("BONDING_UNAVAILABLE_API");
  });

  test("Discovery Failed", () => {
    expect(BluetoothError.DISCOVERY_FAILED).toBe("DISCOVERY_FAILED");
  });

  test("Write Failed", () => {
    expect(BluetoothError.WRITE_FAILED).toBe("WRITE_FAILED");
  });

  test("read Failed", () => {
    expect(BluetoothError.READ_FAILED).toBe("READ_FAILED");
  });
  
  test("Accepting Cancelled", () => {
    expect(BluetoothError.ACCEPTING_CANCELLED).toBe("ACCEPTING_CANCELLED");
  });
  
  test("Connetion Failed", () => {
    expect(BluetoothError.CONNECTION_FAILED).toBe("CONNECTION_FAILED");
  });
  
  test("Connection Lost", () => {
    expect(BluetoothError.CONNECTION_LOST).toBe("CONNECTION_LOST");
  });
  
  test("Pairing Failed", () => {
    expect(BluetoothError.PAIRING_FAILED).toBe("PAIRING_FAILED");
  });  
  
});
