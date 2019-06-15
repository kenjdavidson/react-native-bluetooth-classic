
package kjd.reactnative.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
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
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

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
        implements ActivityEventListener, LifecycleEventListener {

  private static final String TAG = "BluetoothClassicModule";

  private static final boolean D = BuildConfig.DEBUG;

  private BluetoothAdapter mBluetoothAdapter;

  private RNBluetoothClassicService mBluetoothService;

  private ReactApplicationContext mReactContext;

  /**
   * Intent receiver responsible for handling changes to BluetoothAdapter state (on/off).  Fires
   * an event to the ReactNative emitter based on the new state.
   * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_STATE_CHANGED
   */
  final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();

      if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        switch (state) {
          case BluetoothAdapter.STATE_OFF:
            if (D) Log.d(TAG, "Bluetooth was disabled");
            sendEvent(BTEvent.BLUETOOTH_DISABLED.code, null);
            break;
          case BluetoothAdapter.STATE_ON:
            if (D) Log.d(TAG, "Bluetooth was enabled");
            sendEvent(BTEvent.BLUETOOTH_ENABLED.code, null);
            break;
        }
      }
    }
  };

  /**
   * Intent receiver responsible for handling changes to Bluetooth connections.  This Intent is
   * fired when the BluetoothAdapter connection state to any device changes.  It fires an event
   * to the ReactNative emitter containing the state and deviceId which was connected.
   * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_CONNECTION_STATE_CHANGED
   */
  final BroadcastReceiver mBluetoothConnectionReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();

      if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (D) Log.d(TAG, "Device connected: " + device.toString());
        sendEvent(BTEvent.BLUETOOTH_DISCONNECTED.code,  deviceToWritableMap(device));
      }
    }
  };

  /**
   * Delimeter used while reading.  It's possible the buffer may contain more than a single message
   * when this occurs, the specified mDelimiter will be used to split, and cause multiple read
   * events.  With manual reading, the last instance of the mDelimiter will be used.  For example
   * if sending the command {@code ri} to retrieve the Reader Information, then a number of lines
   * will be returned, therefore we can't split on the first "\n".
   * <p>
   * Defaults to "\n"
   */
  private String mDelimiter;

  /**
   * Used to read/write data from the Connected bluetooth device.
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
   * Creates a new RNBluetoothClassicModule.  Attempts to get the BluetoothAdapter from the
   * Android system and initialize the RNBluetoothClassicService.  Finally sends appropriate
   * events to Javascript and registers itself for the appropriate Android events.
   *
   * @param reactContext react native context
   */
  public RNBluetoothClassicModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.mReactContext = reactContext;
    this.mDelimiter = "\n";

    if (mBluetoothAdapter == null) {
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    if (mBluetoothService == null) {
      mBluetoothService = new RNBluetoothClassicService(this);
    }

    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
      sendEvent(BTEvent.BLUETOOTH_ENABLED.code, null);
    } else {
      sendEvent(BTEvent.BLUETOOTH_DISABLED.code, null);
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
    Map<String, Object> constants = super.getConstants();
    if (constants == null) constants = new HashMap<>();
    constants.put("BTEvents", BTEvent.eventNames());
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

    if (requestCode == BTRequest.ENABLE_BLUETOOTH.code) {
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

    if (requestCode == BTRequest.PAIR_DEVICE.code) {
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

  /**
   * Pair a specific device.  This will attempt to pair the device and register a single purpose
   * listener for when the device is paired/failed.
   * <p>
   * This may need to be deprecated in the future, as this was only for kitkat (from the docs)
   * and I don't particularly see a purpose for it now.  It would be better off having a general
   * purpose listener that will let us know when any device has just bonded.
   *
   * @param device Device
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
   * Send event to javascript.
   * <p>
   * TODO pull this into it's own class incase the library gets extended
   *
   * @param eventName Name of the event
   * @param params Additional params
   */
  private void sendEvent(String eventName, @Nullable WritableMap params) {
    if (mReactContext.hasActiveCatalystInstance()) {
      if (D) Log.d(TAG, String.format("Sending event [%s] with data [%s]", eventName, params));
      mReactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit(eventName, params);
    }
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
        activity.startActivityForResult(intent, BTRequest.ENABLE_BLUETOOTH.code);
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
        WritableMap device = deviceToWritableMap(rawDevice);
        deviceList.pushMap(device);
      }
    }
    if (D) Log.d(TAG, "Devices = " + deviceList.toString());
    promise.resolve(deviceList);
  }

  /**
   * Attempt to discover unpaired devices.
   *
   * @param promise
   */
  @ReactMethod
  public void discoverUnpairedDevices(final Promise promise) {
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
   * Attempts to cancel the discovery process.
   *
   * @param promise
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
   * @param id
   * @param promise
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
   * Attempts to connect to the device with the provided Id.
   *
   * @param id of the requested device
   * @param promise resolve or reject the requested connection
   */
  @ReactMethod
  public void connect(String id, Promise promise) {
    mConnectedPromise = promise;
    if (mBluetoothAdapter != null) {
      BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(id);
      if (device != null) {
        mBluetoothService.connect(device);
      } else {
        promise.reject(new Exception("No device found with id " + id));
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
      promise.resolve(deviceToWritableMap(mBluetoothService.connectedDevice()));
    }
    promise.reject(new Error("No bluetooth devices connected"));
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
   * @param promise
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
   * @param delimiter
   * @param promise
   */
  @ReactMethod
  public void readUntilDelimiter(String delimiter, Promise promise) {
    promise.resolve(readUntil(delimiter));
  }

  /**
   * Attempts to read from the device buffer - using the registered delimiter.
   *
   * @param promise
   */
  @ReactMethod
  public void readUntilDelimiter(Promise promise) {
    promise.resolve(readUntil(mDelimiter));
  }

  /**
   * Sets a new delimiter.
   *
   * @param delimiter
   * @param promise
   */
  @ReactMethod
  public void setDelimiter(String delimiter, Promise promise) {
    this.mDelimiter = delimiter;
    promise.resolve(true);
  }

  /**
   * Clears the buffer.
   *
   * @param promise
   */
  @ReactMethod
  public void clear(Promise promise) {
    mBuffer.setLength(0);
    promise.resolve(true);
  }

  /**
   * Gets the available information within the buffer.
   * @param promise
   */
  @ReactMethod
  public void available(Promise promise) {
    promise.resolve(mBuffer.length());
  }


  /**
   * Attempts to set the BluetoothAdapter name.
   *
   * @param newName
   * @param promise
   */
  @ReactMethod
  public void setAdapterName(String newName, Promise promise) {
    if (mBluetoothAdapter != null) {
      mBluetoothAdapter.setName(newName);
    }
    promise.resolve(true);
  }

  /**
   * Resolves the successful connection by returning the Device which was just connected to.
   *
   * @param device the device to which the connection was successful
   */
  void onConnectionSuccess(BluetoothDevice device) {
    // Global device connection events have been moved to the BLUETOOTH_CONNECTED event which is
    // now a module registration.  I don't see it making sense to return a module event as well
    // as a resolved promise, since this should only ever be called after a request to a specific
    // device.
    //sendEvent(BTEvent.CONNECTION_SUCCESS.code, null);
    if (mConnectedPromise != null) {
      mConnectedPromise.resolve(deviceToWritableMap(device));
    }
    mConnectedPromise = null;
  }

  /**
   * Rejects the connection attempt by
   *
   * @param device the device to which the connection was failed
   */
  void onConnectionFailed(BluetoothDevice device) {
    // Global device connection events have been moved to the BLUETOOTH_DISCONNECTED event which is
    // now a module registration.  I don't see it making sense to return a module event as well
    // as a resolved promise, since this should only ever be called after a request to a specific
    // device.
    //sendEvent(BTEvent.CONNECTION_FAILED.code, null);
    if (mConnectedPromise != null) {
      mConnectedPromise.reject(new Exception("Connection unsuccessful"), deviceToWritableMap(device));
    }
    mConnectedPromise = null;
  }

  /**
   * Handle lost connection.  Unlike connectionSuccess and connectionFailed it is important
   * that we manage a lost connection, as we may want to ensure it's still connected (bonded)
   * and then re-connect (socket) to it.
   *
   * @param device the Device to which the connecion was lost
   */
  void onConnectionLost (BluetoothDevice device) {
    WritableMap params = Arguments.createMap();
    params.putMap("device", deviceToWritableMap(device));
    params.putString("message", "Connection unsuccessful");
    sendEvent(BTEvent.CONNECTION_LOST.code, params);
  }

  /**
   * Handle error.
   *
   * @param e Exception
   */
  void onError (Exception e) {
    WritableMap params = Arguments.createMap();
    params.putString("message", e.getMessage());
    sendEvent(BTEvent.ERROR.code, params);
  }

  /**
   * Handle read data.  Updates the buffer with the latest information, then sends the first
   * available chunk of buffer to the BTEvent.READ event.
   *
   * @param data Message
   */
  void onData (String data) {
    mBuffer.append(data);
    String completeData = readUntil(this.mDelimiter);
    if (completeData != null && completeData.length() > 0) {
      WritableMap params = Arguments.createMap();
      params.putString("data", completeData);
      sendEvent(BTEvent.READ.code, params);
    }
  }

  /**
   * Attempts to read from to the first (or if none end) delimiter.
   *
   * @param delimiter
   * @return
   */
  private String readUntil(String delimiter) {
    String data = "";
    int index = mBuffer.indexOf(delimiter, 0);
    if (index > -1) {
      data = mBuffer.substring(0, index + delimiter.length());
      mBuffer.delete(0, index + delimiter.length());
    }
    return data;
  }

  /**
   * Convert BluetoothDevice into WritableMap.
   *
   * @param device Bluetooth device
   */
  private WritableMap deviceToWritableMap(BluetoothDevice device) {
    WritableMap params = Arguments.createMap();

    params.putString("name", device.getName());
    params.putString("address", device.getAddress());
    params.putString("id", device.getAddress());

    if (device.getBluetoothClass() != null) {
      params.putInt("class", device.getBluetoothClass().getDeviceClass());
    }

    return params;
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
        if (deviceId.equals(device.getAddress())) {
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
              Log.e(TAG, "Cannot unregister receiver", e);
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
          WritableMap d = deviceToWritableMap(device);
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

}