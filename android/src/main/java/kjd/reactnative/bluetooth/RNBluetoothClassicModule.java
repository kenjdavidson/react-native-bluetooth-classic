
package kjd.reactnative.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import kjd.reactnative.CommonCharsets;
import kjd.reactnative.RCTEventEmitter;

/**
 * Provides bridge between native Android functionality and React Native javascript.  Provides
 * @ReactMethod methods to Javascript to allow controlling/monitoring:
 * <ul>
 *  <li>the Android Bluetooth configuration status</li>
 *  <li>connecting/disconnecting to specific devices</li>
 *  <li>sending and receiving manual data</li>
 *  <li>receiving pushed messages from a connected device</li>
 * </ul>
 * For more details on React Native modules see:
 * <ul>
 *     <li>https://facebook.github.io/react-native/docs/native-modules-setup</li>
 *     <li>https://facebook.github.io/react-native/docs/native-modules-android</li>
 * </ul>
 * Connect/Disconnect are used in two ways here - which was a little confusing off the bat - but
 * essentially we need to be careful with whether we're dealing with:
 * <ul>
 *     <li>The connection between the BluetoothAdapter and the BluetoothDevice which could be called
 *     bonding (although that's not completely accurate either)</li>
 *     <li>The socket connection between the BluetoothDevice and the application</li>
 * </ul>
 *
 * @author kenjdavidson
 *
 */
public class RNBluetoothClassicModule
        extends ReactContextBaseJavaModule
        implements ActivityEventListener,
            LifecycleEventListener,
            RCTEventEmitter,
            BluetoothEventListener {

  private static final String TAG = RNBluetoothClassicModule.class.getSimpleName();

  private static final boolean D = BuildConfig.DEBUG;

  /**
   * Android {@link BluetoothAdapter} responsible for communication.
   */
  private BluetoothAdapter mBluetoothAdapter;

  /**
   * Responsible for connection and communication with specific device.
   */
  private RNBluetoothClassicService mBluetoothService;

  /**
   * React Native application context.  Allows for event management and listening within the React
   * Native environment.
   */
  private ReactApplicationContext mReactContext;

  /**
   * Intent receiver responsible for handling changes to BluetoothAdapter state (on/off).  Fires
   * an event to the ReactNative emitter based on the new state.
   * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_STATE_CHANGED
   */
  private final BroadcastReceiver mBluetoothStateReceiver = new BluetoothStateReceiver(this);

  /**
   * Intent receiver responsible for handling changes to Bluetooth connections.  This Intent is
   * fired when the BluetoothAdapter connection state to any device changes.  It fires an event
   * to the ReactNative emitter containing the state and deviceId which was connected.
   * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_CONNECTION_STATE_CHANGED
   */
  private final BroadcastReceiver mBluetoothConnectionReceiver = new BluetoothConnectionReceiver(this);

  /**
   * Delimiter used while reading.  It's possible the buffer may contain more than a single message
   * when this occurs, the specified mDelimiter will be used to split, and cause multiple read
   * events.  With manual reading, the last instance of the mDelimiter will be used.  For example
   * if sending the command {@code ri} to retrieve the Reader Information, then a number of lines
   * will be returned, therefore we can't split on the first "\n".
   * <p>
   * Defaults to "\n"
   */
  private String mDelimiter;

  /**
   * Default charset for Bluetooth communication.  Defaults to LATIN / ISO_8859_1 as it was in
   * the original react-native-bluetooth-serial project.  This can be set using the
   *
   */
  private Charset mCharset;

  /**
   * Whether there are BTEvent.READ listeners on the NativeEventEmitter.js side of things.  This
   * will control whether the onData method passes through to NativeEventEmitter.js
   */
  private AtomicBoolean mReadObserving = new AtomicBoolean(false);

  /**
   * Used to read/write data from the Connected bluetooth device.  This should eventually be moved
   * into the RNBluetoothClassicService to create a more similar pattern to IOS.
   */
  private StringBuffer mBuffer = new StringBuffer();

  /**
   * Resolve or reject Bluetooth enable request.  Due to the request needing to start a new Intent
   * and wait for the Activity result, the Promise must be maintained.
   */
  private Promise mEnabledPromise;

  /**
   * Resolve or reject connection request.  Due to the request needing to start a new Intent
   * and wait for the Activity result, the Promise must be maintained.
   */
  private Promise mConnectedPromise;

  /**
   * Resolve or reject requested discovery.  Due to the request needing to start a new Intent
   * and wait for the Activity result, the Promise must be maintained.
   */
  private Promise mDeviceDiscoveryPromise;

  /**
   * Resolve or reject device pairing request.  Due to the request needing to start a new Intent
   * and wait for the Activity result, the Promise must be maintained.
   */
  private Promise mPairDevicePromise;

  /**
   * Creates a new RNBluetoothClassicModule using the standard delimiter and charset.
   *
   * @param reactContext react native context
   */
  public RNBluetoothClassicModule(ReactApplicationContext reactContext) {
    this(reactContext, "\n", CommonCharsets.LATIN.charset());
  }

  /**
   * Creates a new {@link RNBluetoothClassicModule} with a custom default delimiter and charset.
   * Attempts to get the BluetoothAdapter from the Android system and initialize the
   * {@link RNBluetoothClassicService}.  Finally sends appropriate events to Javascript and registers
   * itself for the appropriate Android events.
   *
   * @param reactContext React Native context
   * @param delimiter default delimiter
   * @param charset default charset
   */
  public RNBluetoothClassicModule(ReactApplicationContext reactContext,
                                  String delimiter,
                                  Charset charset) {
    super(reactContext);

    this.mReactContext = reactContext;
    this.mDelimiter = delimiter;
    this.mCharset = charset;

    if (mBluetoothAdapter == null) {
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    if (mBluetoothService == null) {
      mBluetoothService = new RNBluetoothClassicService(this);
    }

    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
      sendEvent(BluetoothEvent.BLUETOOTH_ENABLED.code, null);
    } else {
      sendEvent(BluetoothEvent.BLUETOOTH_DISABLED.code, null);
    }

    mReactContext.addActivityEventListener(this);
    mReactContext.addLifecycleEventListener(this);
  }

  @Override
  public String getName() {
    return "RNBluetoothClassic";
  }

  @Override
  public boolean hasConstants() {
    return true;
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    Map<String,Object> constants = new HashMap<>();
    constants.put("BTEvents", BluetoothEvent.eventNames());
    constants.put("BTCharsets", CommonCharsets.asMap());
    return constants;
  }

  /**
   * Handles results from the requested Android Intents.
   * <ul>
   *     <li>1 result from bluetooth enable request</li>
   *     <li>2 result from bluetooth pair request</li>
   * </ul>
   *
   * @param activity the activity which is returning the result
   * @param requestCode request code provided to the outgoing intent
   * @param resultCode result of the requested Intent
   * @param data the intent which triggered this result
   */
  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    if (D) Log.d(TAG, "On activity result request: " + requestCode + ", result: " + resultCode);

    if (requestCode == BluetoothRequest.ENABLE_BLUETOOTH.code) {
      if (resultCode == Activity.RESULT_OK) {
        if (D) Log.d(TAG, "User enabled Bluetooth");
        if (mEnabledPromise != null) {
          mEnabledPromise.resolve(true);
        }
      } else {
        if (D) Log.d(TAG, "User did *NOT* enable Bluetooth");
        if (mEnabledPromise != null) {
          mEnabledPromise.reject(new Exception("User did not enable Bluetooth"));
        }
      }
      mEnabledPromise = null;
    }

    if (requestCode == BluetoothRequest.PAIR_DEVICE.code) {
      if (resultCode == Activity.RESULT_OK) {
        if (D) Log.d(TAG, "Pairing ok");
        if (mEnabledPromise != null) {
          mPairDevicePromise.resolve(true);
        }
      } else {
        if (D) Log.d(TAG, "Pairing failed");
        if (mEnabledPromise != null) {
          mPairDevicePromise.reject(new Exception("Pairing failed"));
        }
      }
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (D) Log.d(TAG, "On new Intent: " + intent.getAction());
  }

  @Override
  public void onHostResume() {
    if (D) Log.d(TAG, "Resumed - adding receivers");
    registerBluetoothReceivers();
  }

  @Override
  public void onHostPause() {
    if (D) Log.d(TAG, "Pause - removing receivers");
    unregisterBluetoothReceivers();
  }

  @Override
  public void onHostDestroy() {
    if (D) Log.d(TAG, "Host destroy");
    mBluetoothService.stop();
  }

  /**
   * Requests that the Android Bluetooth adapter be enabled.  If the adapter is already enabled
   * then the promise is returned true.  If the adapter is not enabled, an Intent request is sent
   * to Android (promised saved for use upon result).
   *
   * @param promise resolves or rejects the attempt to enable Bluetooth.
   */
  @ReactMethod
  public void requestEnable(Promise promise) {
    // If bluetooth is already enabled resolve promise immediately
    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
      promise.resolve(true);
      // Start new intent if bluetooth is note enabled
    } else {
      Activity activity = getCurrentActivity();
      mEnabledPromise = promise;
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      if (activity != null) {
        activity.startActivityForResult(intent, BluetoothRequest.ENABLE_BLUETOOTH.code);
      } else {
        Exception e = new Exception("Cannot start activity");
        Log.e(TAG, "Cannot start activity", e);
        mEnabledPromise.reject(e);
        mEnabledPromise = null;
        onError(e);
      }
    }
  }

  /**
   * Determine whether Bluetooth is enabled.  The promise is never rejected, only resolved with the
   * appropriate boolean flag.
   *
   * @param promise resolve based on Bluetooth status
   */
  @ReactMethod
  public void isEnabled(Promise promise) {
    if (mBluetoothAdapter != null) {
      promise.resolve(mBluetoothAdapter.isEnabled());
    } else {
      promise.resolve(false);
    }
  }

  /**
   * Lists the currently connected/bonded devices.
   *
   * @param promise resolves the list of bonded devices.
   */
  @ReactMethod
  public void list(Promise promise) {
    WritableArray deviceList = Arguments.createArray();
    if (mBluetoothAdapter != null) {
      Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
      for (BluetoothDevice rawDevice : bondedDevices) {
        WritableMap device = RNUtils.deviceToWritableMap(rawDevice);
        deviceList.pushMap(device);
      }
    }
    if (D) Log.d(TAG, "Devices = " + deviceList.toString());
    promise.resolve(deviceList);
  }

  /**
   * Attempt to discover unpaired devices.  Resolves when the BluetoothDiscoveryReceiver
   * returns.
   *
   * @param promise resolve or reject the request to discoverDevices
   */
  @ReactMethod
  public void discoverDevices(final Promise promise) {
    if (D) Log.d(TAG, "Discover unpaired called");

    mDeviceDiscoveryPromise = promise;
    registerBluetoothDeviceDiscoveryReceiver();

    if (mBluetoothAdapter != null) {
      mBluetoothAdapter.startDiscovery();
    } else {
      promise.resolve(Arguments.createArray());
    }
  }

  /**
   * Attempts to cancel the discovery process.  Cancel request is always resolved as true at this
   * point, which may need to be changed, but for now whether anything happens or not it's
   * seen as successful.
   *
   * @param promise resolves cancel request
   */
  @ReactMethod
  public void cancelDiscovery(final Promise promise) {
    if (D) Log.d(TAG, "Cancel discovery called");

    if (mBluetoothAdapter != null) {
      if (mBluetoothAdapter.isDiscovering()) {
        mBluetoothAdapter.cancelDiscovery();
      }
    }
    promise.resolve(true);
  }

  /**
   * Attempts to pair to the requested device Id - retrieves the device from the BluetoothAdapter
   * and attempts the pairing.
   *
   * @param id the Id of the BluetoothDevice to which we are pairing
   * @param promise resolves when the BluetoothDevice is paired, rejects if there are any issues
   */
  @ReactMethod
  public void pairDevice(String id, Promise promise) {
    if (D) Log.d(TAG, "Pair device: " + id);

    if (mBluetoothAdapter != null) {
      mPairDevicePromise = promise;

      BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(id);
      if (device != null) {
        try{
          pairDevice(device);
        } catch(DevicePairingException e) {
          if (mPairDevicePromise != null) {
            mPairDevicePromise.reject(e);
            mPairDevicePromise = null;
          }
          onError(e);
        }
      } else {
        mPairDevicePromise.reject(new Exception("Could not pair device " + id));
        mPairDevicePromise = null;
      }
    } else {
      promise.resolve(false);
    }
  }

  /**
   * Pair a specific device.  This will attempt to pair the device and register a single purpose
   * listener for when the device is paired/failed.
   * <p>
   * This may need to be deprecated in the future, as this was only for kitkat (from the docs)
   * and I don't particularly see a purpose for it now.  It would be better off having a general
   * purpose listener that will let us know when any device has just bonded.
   *
   * @param device BluetoothDevice to which we are pairing
   *
   */
  private void pairDevice(BluetoothDevice device) throws DevicePairingException {
    try {
      if (D) Log.d(TAG, "Start Pairing...");
      Method m = device.getClass().getMethod("createBond", (Class[]) null);
      m.invoke(device, (Object[]) null);
      registerDevicePairingReceiver(device.getAddress(), BluetoothDevice.BOND_BONDED);
      if (D) Log.d(TAG, "Pairing finished.");
    } catch (Exception e) {
      Log.e(TAG, "Cannot pair device", e);
      throw new DevicePairingException(e);
    }
  }

  /**
   * Request that a device be unpaired.  The device Id is required - looked up using the
   * BluetoothAdapter and unpaired.
   */
  @ReactMethod
  public void unpairDevice(String id, Promise promise) {
    if (D) Log.d(TAG, "Unpair device: " + id);

    if (mBluetoothAdapter != null) {
      mPairDevicePromise = promise;
      BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(id);
      if (device != null) {
        try {
          unpairDevice(device);
        } catch(DevicePairingException e) {
          if (mPairDevicePromise != null) {
            mPairDevicePromise.reject(e);
            mPairDevicePromise = null;
          }
          onError(e);
        }
      } else {
        mPairDevicePromise.reject(new Exception("Could not unpair device " + id));
        mPairDevicePromise = null;
      }
    } else {
      promise.resolve(false);
    }
  }

  /**
   * Performs the unpairing of the requested device.
   *
   * @param device to be unpaired
   */
  private void unpairDevice(BluetoothDevice device) throws DevicePairingException {
    try {
      if (D) Log.d(TAG, "Start Unpairing...");
      Method m = device.getClass().getMethod("removeBond", (Class[]) null);
      m.invoke(device, (Object[]) null);
      registerDevicePairingReceiver(device.getAddress(), BluetoothDevice.BOND_NONE);
    } catch (Exception e) {
      Log.e(TAG, "Cannot unpair device", e);
      throw new DevicePairingException(e);
    }
  }

  /**
   * Attempts to connect to the device with the provided Id.
   *
   * @param id of the requested device
   * @param promise resolve or reject the requested connection
   */
  @ReactMethod
  public void connect(String id, Promise promise) {
    mConnectedPromise = promise;
    if (mBluetoothAdapter != null) {
      try {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(id);
        if (device != null) {
          mBluetoothService.connect(device);
        } else {
          promise.reject(new Exception("No device found with id " + id));
        }
      } catch (Exception e) {
        promise.reject(new Exception("Unable to connect to device"));
        Log.e(TAG, "Unable to connect to device", e);
        onError(e);
      }
    } else {
      promise.reject(new Exception("BluetoothAdapter is not enabled"));
    }
  }

  /**
   * Disconnect the BluetoothService from the currently connected device.
   *
   * @param promise resolve or reject the disconnect request
   */
  @ReactMethod
  public void disconnect(Promise promise) {
    mBluetoothService.stop();
    promise.resolve(true);
  }

  /**
   * Check to see if the Bluetooth service has a connected device.
   *
   * @param promise resolved with the connected status
   */
  @ReactMethod
  public void isConnected(Promise promise) {
    promise.resolve(mBluetoothService.isConnected());
  }

  @ReactMethod
  public void getConnectedDevice(Promise promise) {
    if (mBluetoothService.isConnected()) {
      promise.resolve(RNUtils.deviceToWritableMap(mBluetoothService.connectedDevice()));
    }
    promise.reject(new Error("No bluetooth device connected"));
  }


  /**
   * Attempts to write to the device.  The message string should be encoded as Base64
   *
   * @param message base64 encoded message to be sent
   * @param promise resolved once the message has been written.
   */
  @ReactMethod
  public void writeToDevice(String message, Promise promise) {
    if (D) Log.d(TAG, "Write " + message);
    byte[] data = Base64.decode(message, Base64.DEFAULT);
    mBluetoothService.write(data);
    promise.resolve(true);
  }

  /**
   * Attempts to read from the device.  The full buffer is read (then cleared) without using the
   * mDelimiter.  Note - there will never be data within the buffer if the application is currently
   * registered to receive read events.
   *
   * @param promise resolves with data, could be null or 0 length
   */
  @ReactMethod
  public void readFromDevice(Promise promise) {
    if (D) Log.d(TAG, "Read");
    int length = mBuffer.length();
    String data = mBuffer.substring(0, length);
    mBuffer.delete(0, length);
    promise.resolve(data);
  }

  /**
   * Attempts to read from the device buffer - until the first instance of the mDelimiter.  Allows
   * for a different mDelimiter to be used than what was originally registered.
   *
   * @param delimiter the delimiter to use for parsing this request, in case a one time delimiter
   *                  is required.
   * @param promise resolves with data, could be null or 0 length
   */
  @ReactMethod
  public void readUntilDelimiter(String delimiter, Promise promise) {
    promise.resolve(readUntil(delimiter));
  }

  /**
   * Sets a new delimiter.
   *
   * @param delimiter the new delimiter to be used for parsing buffer
   * @param promise resolves true
   */
  @ReactMethod
  public void setDelimiter(String delimiter, Promise promise) {
    this.mDelimiter = delimiter;
    promise.resolve(true);
  }

  /**
   * Sets the default charset for subsequent connections.
   *
   * @param code charset code to which we attempt to set the default
   * @param promise resolves if the code successfully set, rejects if the code is not known
   */
  @ReactMethod
  public void setEncoding(String code, Promise promise) {
    try {
      this.mCharset = CommonCharsets.valueOf(code).charset();
      promise.resolve(true);
    } catch(EnumConstantNotPresentException e) {
      promise.reject(e);
    }
  }

  /**
   * Configures whether or not we are Observing read events.  Would have liked to use start/stop
   * Observing, but that would require overriding both Android and IOS, as well as possibly
   * affecting future functionality.
   *
   * @param readObserving whether React Native has a READ listener
   * @param promise resolves true
   */
  @ReactMethod
  public void setReadObserving(boolean readObserving, Promise promise) {
    this.mReadObserving.set(readObserving);
    promise.resolve(true);
  }

  /**
   * Clears the buffer.
   *
   * @param promise resolves true
   */
  @ReactMethod
  public void clear(Promise promise) {
    mBuffer.setLength(0);
    promise.resolve(true);
  }

  /**
   * Gets the available information within the buffer.  There is no concept of the delimiter in
   * this request - which may need to be changed - since in most cases I can see a full message
   * needing to be available.
   *
   * @param promise resolves with length of buffer, could be 0
   */
  @ReactMethod
  public void available(Promise promise) {
    promise.resolve(mBuffer.length());
  }

  /**
   * Attempts to set the BluetoothAdapter name.
   *
   * @param newName the name to which the adapter will be changed
   * @param promise resolves true
   */
  @ReactMethod
  public void setAdapterName(String newName, Promise promise) {
    if (mBluetoothAdapter != null) {
      mBluetoothAdapter.setName(newName);
    }
    promise.resolve(true);
  }

  @Override
  public void onConnectionSuccess(BluetoothDevice device) {
    // Global device connection events have been moved to the BLUETOOTH_CONNECTED event which is
    // now a module registration.  I don't see it making sense to return a module event as well
    // as a resolved promise, since this should only ever be called after a request to a specific
    // device.
    sendEvent(BluetoothEvent.CONNECTION_SUCCESS.code, RNUtils.deviceToWritableMap(device));
    if (mConnectedPromise != null) {
      mConnectedPromise.resolve(RNUtils.deviceToWritableMap(device));
    }
    mConnectedPromise = null;
  }

  @Override
  public void onConnectionFailed(BluetoothDevice device, Throwable reason) {
    String msg = String.format("Connection to device %s has failed", device.getName());
    Log.d(this.getClass().getSimpleName(),  msg, reason);

    if (mConnectedPromise != null) {
      mConnectedPromise.reject(new Exception("Connection unsuccessful"), RNUtils.deviceToWritableMap(device));
    }

    mConnectedPromise = null;
  }

  @Override
  public void onConnectionLost (BluetoothDevice device, Throwable reason) {
    String msg = String.format("Connection to device %s was lost", device.getName());
    Log.d(this.getClass().getSimpleName(),  msg, reason);

    WritableMap params = Arguments.createMap();
    params.putMap("device", RNUtils.deviceToWritableMap(device));
    params.putString("message", msg);
    params.putString("error", reason.getMessage());
    sendEvent(BluetoothEvent.CONNECTION_LOST.code, params);
  }

  @Override
  public void onError (Throwable e) {
    Log.e(this.getClass().getSimpleName(), e.getMessage(), e);

    WritableMap params = Arguments.createMap();
    params.putString("message", e.getMessage());
    params.putString("error", e.getMessage());
    params.putMap("device", RNUtils.deviceToWritableMap(mBluetoothService.connectedDevice()));
    sendEvent(BluetoothEvent.ERROR.code, params);
  }

  /**
   * Handles the data input from the device.  First it appends the new data to the buffer, then
   * it attempts to get all the data segments (delimiter) based on what is available.  This is
   * important as it may be possible that the device writes multiple lines to the buffer at one
   * time and we don't want to miss any of the messages just because we only read to the first
   * instance of the delimiter.
   * <p>
   * This became apparent when sending a "ri\r" or "help\r" and only getting back the first few
   * lines with the test device.  The buffer still had content though.
   * <p>
   * We may need to take this a step further and customize how data is returned.  For example,
   * should each of the items be returned separately, or all at once?
   *
   * @param data Message
   */
  @Override
  public void onReceivedData(BluetoothDevice device, byte[] data) {
    String msg = String.format("Received %d bytes from device %s", data.length, device.getName());
    Log.d(TAG, msg);

    mBuffer.append(new String(data, mCharset));

    if (!mReadObserving.get()) {
      Log.d(TAG, "No BTEvent.READ listeners are registered, skipping handling of the event");
      return;
    }

    String message;
    while ((message = readUntil(this.mDelimiter)) != null) {
      BluetoothMessage bluetoothMessage
              = new BluetoothMessage<>(RNUtils.deviceToWritableMap(mBluetoothService.connectedDevice()), message);
      sendEvent(BluetoothEvent.READ.code, bluetoothMessage.asMap());
    }
  }

  /**
   * Attempts to read from to the first (or if none end) delimiter.  If the delimiter is found
   * then the data is retrieved and removed from the buffer.
   *
   * @param delimiter value to use as the reading delimiter
   * @return the data up to the next delimiter
   */
  private String readUntil(String delimiter) {
    String data = null;
    int index = mBuffer.indexOf(delimiter, 0);
    if (index > -1) {
      int len = index + delimiter.length();
      data = mBuffer.substring(0, len);
      mBuffer.delete(0, len);
    }
    return data;
  }

  /**
   * Register receivers:
   * <ul>
   *     <li>ACTION_STATE_CHANGED - when on/off</li>
   *     <li>ACTION_CONNECTION_STATE_CHANGED - when device connection changes</li>
   * </ul>
   */
  private void registerBluetoothReceivers() {
    IntentFilter stateIntent = new IntentFilter();
    stateIntent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    mReactContext.registerReceiver(mBluetoothStateReceiver, stateIntent);

    IntentFilter connIntent = new IntentFilter();
    connIntent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
    mReactContext.registerReceiver(mBluetoothConnectionReceiver, connIntent);
  }

  /**
   * Unregister the receivers.
   */
  private void unregisterBluetoothReceivers() {
    mReactContext.unregisterReceiver(mBluetoothStateReceiver);
    mReactContext.unregisterReceiver(mBluetoothConnectionReceiver);
  }

  /**
   * Register a single serving ACTION_BOND_STATE_CHANGED receiving listening for a specific
   * device.  If the device requested isn't the device bonded we do not want to resolve the
   * promise - it seems unlikely, but is possible.
   *
   * @param deviceId Id of device
   * @param requiredState State that we require
   */
  private void registerDevicePairingReceiver(final String deviceId, final int requiredState) {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

    final BroadcastReceiver devicePairingReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (!deviceId.equals(device.getAddress())) {
            if (D) Log.d(TAG, String.format("Bonding device [%s] does not match expected [%s]",
                    device.getAddress(), deviceId));
            return;
        }

        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
          final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
          final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

          if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
            if (D) Log.d(TAG, "Device paired");
            if (mPairDevicePromise != null) {
              mPairDevicePromise.resolve(true);
              mPairDevicePromise = null;
            }
            try {
              mReactContext.unregisterReceiver(this);
            } catch (Exception e) {
              onError(e);
            }
          } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
            if (D) Log.d(TAG, "Device unpaired");
            if (mPairDevicePromise != null) {
              mPairDevicePromise.resolve(true);
              mPairDevicePromise = null;
            }
            try {
              mReactContext.unregisterReceiver(this);
            } catch (Exception e) {
              Log.e(TAG, "Cannot unregister receiver", e);
              onError(e);
            }
          }

        }
      }
    };

    mReactContext.registerReceiver(devicePairingReceiver, intentFilter);
  }

  /**
   * Register receiver for bluetooth device discovery
   */
  private void registerBluetoothDeviceDiscoveryReceiver() {
    IntentFilter intentFilter = new IntentFilter();

    intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

    final BroadcastReceiver deviceDiscoveryReceiver = new BroadcastReceiver() {
      private WritableArray unpairedDevices = Arguments.createArray();
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (D) Log.d(TAG, "onReceive called");

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
          BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
          WritableMap d = RNUtils.deviceToWritableMap(device);
          unpairedDevices.pushMap(d);
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
          if (D) Log.d(TAG, "Discovery finished");
          if (mDeviceDiscoveryPromise != null) {
            mDeviceDiscoveryPromise.resolve(unpairedDevices);
            mDeviceDiscoveryPromise = null;
          }

          try {
            mReactContext.unregisterReceiver(this);
          } catch (Exception e) {
            Log.e(TAG, "Unable to unregister receiver", e);
            onError(e);
          }
        }
      }
    };

    mReactContext.registerReceiver(deviceDiscoveryReceiver, intentFilter);
  }

  @Override
  public ReactContext getReactContext() {
    return mReactContext;
  }
}
