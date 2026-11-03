import { Platform } from 'react-native';

import BluetoothModule from '../src/BluetoothModule';
import RNBluetoothClassicModule from '../src/BluetoothNativeModule';

jest.mock('react-native', () => ({
  Platform: { OS: 'ios' },
  NativeEventEmitter: class {
    addListener() {
      return { remove: () => {} };
    }
  },
}));

describe('React Native Platform', () => {
  test("Platform.OS should be 'ios'", () => {
    expect(Platform.OS).toBe('ios');
  });
});

describe('BluetoothModule Android only features', () => {
  let bluetoothModule: BluetoothModule;

  beforeEach(() => {
    // the native module is never reached on iOS: every method below short circuits first
    bluetoothModule = new BluetoothModule({} as RNBluetoothClassicModule);
  });

  // Each of these declares `Promise<...>`, so an unsupported platform has to be reported as a
  // rejection. Throwing before returning a promise means a caller's `.catch()` never attaches and
  // the error escapes into surrounding code instead.
  test('startDiscovery should reject', async () => {
    await expect(bluetoothModule.startDiscovery()).rejects.toThrow('Method not implemented.');
  });

  test('cancelDiscovery should reject', async () => {
    await expect(bluetoothModule.cancelDiscovery()).rejects.toThrow('Method not implemented.');
  });

  test('pairDevice should reject', async () => {
    await expect(bluetoothModule.pairDevice('AA:BB:CC:DD:EE:FF')).rejects.toThrow('Method not implemented.');
  });

  test('unpairDevice should reject', async () => {
    await expect(bluetoothModule.unpairDevice('AA:BB:CC:DD:EE:FF')).rejects.toThrow('Method not implemented.');
  });

  test('accept should reject', async () => {
    await expect(bluetoothModule.accept({})).rejects.toThrow('Method not implemented.');
  });

  test('cancelAccept should reject', async () => {
    await expect(bluetoothModule.cancelAccept()).rejects.toThrow('Method not implemented.');
  });

  test('requestBluetoothEnabled should reject', async () => {
    await expect(bluetoothModule.requestBluetoothEnabled()).rejects.toThrow('Method not implemented.');
  });

  test('setBluetoothAdapterName should reject', async () => {
    await expect(bluetoothModule.setBluetoothAdapterName('name')).rejects.toThrow('Method not implemented.');
  });

  // The regression this guards: a caller opting out of an Android-only step needs `.catch()` on the
  // returned value to run. That only happens if a promise is returned at all.
  test('a rejection handler attached to the returned promise runs', async () => {
    const handled = await bluetoothModule.cancelDiscovery().catch(() => 'handled');

    expect(handled).toBe('handled');
  });

  // `openBluetoothSettings` returns void, so it has no promise to reject - a synchronous throw is
  // correct here and is asserted to keep the distinction deliberate.
  test('openBluetoothSettings should throw synchronously', () => {
    expect(() => bluetoothModule.openBluetoothSettings()).toThrow('Method not implemented.');
  });
});
