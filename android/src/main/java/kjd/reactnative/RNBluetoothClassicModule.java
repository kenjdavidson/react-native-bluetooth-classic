
package kjd.reactnative;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import kjd.reactnative.bluetooth.event.BluetoothEvent;
import kjd.reactnative.bluetooth.BluetoothException;
import kjd.reactnative.bluetooth.BluetoothMessage;
import kjd.reactnative.bluetooth.BluetoothRequest;
import kjd.reactnative.bluetooth.BluetoothState;
import kjd.reactnative.bluetooth.BuildConfig;
import kjd.reactnative.bluetooth.ConnectionFailedException;
import kjd.reactnative.bluetooth.ConnectionLostException;
import kjd.reactnative.bluetooth.DevicePairingException;
import kjd.reactnative.bluetooth.Exceptions;
import kjd.reactnative.bluetooth.InvalidBluetoothEventException;
import kjd.reactnative.bluetooth.RNUtils;
import kjd.reactnative.bluetooth.conn.ConnectionProperty;
import kjd.reactnative.bluetooth.conn.ConnectionType;
import kjd.reactnative.bluetooth.device.DeviceConnectionListener;
import kjd.reactnative.bluetooth.device.DataReceivedListener;
import kjd.reactnative.bluetooth.device.DeviceConnection;
import kjd.reactnative.bluetooth.device.DeviceConnectionFactory;
import kjd.reactnative.bluetooth.device.NativeDevice;
import kjd.reactnative.bluetooth.event.BluetoothStateChangeEvent;
import kjd.reactnative.bluetooth.receiver.ActionACLReceiver;
import kjd.reactnative.bluetooth.receiver.DiscoveryReceiver;
import kjd.reactnative.bluetooth.receiver.PairingReceiver;
import kjd.reactnative.bluetooth.receiver.StateChangeReceiver;

/**
 * Provides bridge between native Android functionality and React Native javascript.  Provides
 * {@code @ReactMethod} methods to Javascript to allow controlling/monitoring:
 * <ul>
 *  <li>the Android Bluetooth configuration status</li>
 *  <li>connecting/disconnecting to specific devices</li>
 *  <li>sending and receiving manual data</li>
 *  <li>receiving pushed messages from a connected device</li>
 * </ul>
 * For more details on React Native modules see:
 * <ul>
 *  <li>https://facebook.github.io/react-native/docs/native-modules-setup</li>
 *  <li>https://facebook.github.io/react-native/docs/native-modules-android</li>
 * </ul>
 * The {@link RNBluetoothClassicModule} is configured with a number of different connection
 * types (this is done through {@link DeviceConnectionFactory}(s).  The standard factories are
 * ACCEPT and CONNECT which use delimited data handlers.
 *
 * @author kendavidson
 */
@SuppressWarnings("unused")
public class RNBluetoothClassicModule
        extends ReactContextBaseJavaModule
        implements ActivityEventListener,
        LifecycleEventListener,
        DeviceConnectionListener,
        DataReceivedListener,
        StateChangeReceiver.StateChangeCallback,
        ActionACLReceiver.ActionACLCallback {

    /**
     * React native module name.
     */
    public  static final String MODULE_NAME = "RNBluetoothClassic";

    /**
     * Logging definition.
     */
    private static final String TAG = RNBluetoothClassicModule.class.getSimpleName();

    /**
     * Local access to the default {@link BluetoothAdapter}.  Generally we just need to check things
     * like:
     * <ul>
     *     <li>Is bluetooth enabled?</li>
     *     <li>Is bluetooth searching / advertising?</li>
     *     <li>Etc.</li>
     * </ul>
     */
    private final BluetoothAdapter mAdapter;

    /**
     * The available {@link DeviceConnectionFactory}(s) that are available to the application.  By
     * default these will contain ACCEPT and CONNECT types.  These can be overridden during the
     * {@link com.facebook.react.ReactPackage} creation process in your {@code MainApplication}.
     */
    private final Map<String,DeviceConnectionFactory> mFactories;

    /**
     * Manages {@link DeviceConnection} wrapping {@link BluetoothDevice} by
     * {@link BluetoothDevice#getAddress()}.  Currently the initial capacity is 1, since the main
     * goal of this was a simple connection.  This may need to be updated to have the default
     * size updated during package creation.
     */
    private Map<String, DeviceConnection> mConnections;

    /**
     * Maintains a Map of attempted connections by {@link BluetoothDevice#getAddress()}.  Once a
     * device converts to an active RFCOMM connection it will be wrapped in a
     * {@link DeviceConnection} and moved into the connections map.  Initial capacity is 2 devices,
     * eventually this should be configurable through the package.
     */
    private Map<String, DeviceConnectionPromise> mConnecting;

    /**
     * Manages intents while the application and {@link BluetoothAdapter} are in discovery mode.
     * This will be cancelled when the application is paused or ends discovery.
     */
    private BroadcastReceiver mDiscoveryReceiver;

    /**
     * Intent receiver responsible for handling changes to BluetoothAdapter state (on/off).  Fires
     * an event to the ReactNative emitter based on the new state.
     * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_STATE_CHANGED
     */
    private BroadcastReceiver mStateChangeReceiver;

    /**
     * Intent receiver responsible for handling changes to Bluetooth connections.  This Intent is
     * fired when the BluetoothAdapter connection state to any device changes.  It fires an event
     * to the ReactNative emitter containing the state and deviceId which was connected.
     * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_CONNECTION_STATE_CHANGED
     */
    private BroadcastReceiver mActionACLReceiver;

    /**
     * Whether there are BTEvent.READ listeners on the NativeEventEmitter.js side of things.  This
     * will control whether the onData method passes through to NativeEventEmitter.js
     */
    private AtomicBoolean mReadObserving = new AtomicBoolean(false);

    /**
     * Promise must be maintained across Activity requests for managing the enabled request
     * status.
     */
    private Promise mEnabledPromise;

    /**
     * Manage the number of listeners of a specific type - these event types are that of
     * the bluetooth adapter in general (connect, disconnect, etc.) and not those which are
     * reading.  Those are managed separately within the device itself.
     */
    private Map<String, AtomicInteger> mListenerCounts;

    //region: Constructors
    public RNBluetoothClassicModule(ReactApplicationContext context,
                                    Map<String,DeviceConnectionFactory> factories) {
        super(context);

        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mFactories = factories;
        this.mConnections = new ConcurrentHashMap<>(1);
        this.mConnecting = new ConcurrentHashMap<>(1);
        this.mListenerCounts = new ConcurrentHashMap<>();

        if (mAdapter != null && mAdapter.isEnabled()) {
            sendEvent(BluetoothEvent.BLUETOOTH_ENABLED,
                    new BluetoothStateChangeEvent(BluetoothState.ENABLED).map());
        } else {
            sendEvent(BluetoothEvent.BLUETOOTH_DISABLED,
                    new BluetoothStateChangeEvent(BluetoothState.DISABLED).map());
        }

        getReactApplicationContext().addActivityEventListener(this);
        getReactApplicationContext().addLifecycleEventListener(this);
    }
    //endregion

    //region: ReactContextBaseJavaModule methods
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public boolean hasConstants() {
        return true;
    }

    /**
     * Provides constants to the React JS environment.
     *
     * @return
     */
    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();
        constants.put("BTEvents", BluetoothEvent.eventNames());
        return constants;
    }
    //endregion

    //region: Helper/Utility Methods
    private boolean checkBluetoothAdapter() {
        return (mAdapter != null && mAdapter.isEnabled());
    }

    private String getLocalAddress() {
        return mAdapter.getAddress();
    }
    //endregion

    //region: ActivityEventListener
    /**
     * Handles results from the requested Android Intents.  Currently there are only two activities
     * started for result:
     * <ul>
     * <li><strong>ENABLE_BLUETOOTH</strong> requests the user to enable Bluetooth from settings.</li>
     * <li><strong>PAIR_DEVICE</strong> after a user has completed pairing the device.</li>
     * </ul>
     * This sends a {@link BluetoothEvent#BLUETOOTH_ENABLED} event.  It probably shouldn't duplicate
     * the promise but this gives the opportunity to do both things.
     *
     * @param activity    the activity which is returning the result
     * @param requestCode request code provided to the outgoing intent
     * @param resultCode  result of the requested Intent
     * @param data        the intent which triggered this result
     */
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, String.format("onActivityResult requestCode: %d resultCode: %d", requestCode, resultCode));

        if (requestCode == BluetoothRequest.ENABLE_BLUETOOTH.code) {
            if (resultCode == Activity.RESULT_OK) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "User enabled Bluetooth");

                if (mEnabledPromise != null) {
                    mEnabledPromise.resolve(true);
                    sendEvent(BluetoothEvent.BLUETOOTH_ENABLED,
                            new BluetoothStateChangeEvent(BluetoothState.ENABLED).map());
                }
            } else {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "User did *NOT* enable Bluetooth");
                if (mEnabledPromise != null) {
                    mEnabledPromise.reject(new Exception("User did not enable Bluetooth"));
                }
            }
            mEnabledPromise = null;
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onNewIntent: " + intent.getAction());
    }
    //endregion

    //region: LifecycleEventListener
    @Override
    public void onHostResume() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onHostResume: register Application receivers");

        registerBluetoothReceivers();
    }

    @Override
    public void onHostPause() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onHostPause: unregister receivers");

        unregisterBluetoothReceivers();
    }

    @Override
    public void onHostDestroy() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onHostDestroy: stop discovery, connections and unregister receivers");

        mAdapter.cancelDiscovery();
    }
    //endregion

    /**
     * Requests that the Android Bluetooth adapter be enabled.  If the adapter is already enabled
     * then the promise is returned true.  If the adapter is not enabled, an Intent request is sent
     * to Android (promised saved for use upon result).
     * <p>
     * Note that this does not inherently fire a state change event, as the manual act seems to
     * skip the StateChangeReceiver functionality.
     *
     * @param promise resolves <strong>true</strong> if Bluetooth is already enabled or when
     *                Bluetooth becomes enabled.  Rejects if anything else
     */
    @ReactMethod
    public void requestBluetoothEnabled(Promise promise) {
        if (checkBluetoothAdapter()) {
            promise.resolve(true);
        } else {
            Activity activity = getCurrentActivity();
            if (activity != null) {
                mEnabledPromise = promise;

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(intent, BluetoothRequest.ENABLE_BLUETOOTH.code);
            } else {
                ActivityNotFoundException e = new ActivityNotFoundException();
                mEnabledPromise.reject(e);
                mEnabledPromise = null;
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
    public void isBluetoothEnabled(Promise promise) {
        promise.resolve(checkBluetoothAdapter());
    }

    /**
     * Retrieves the currently bonded devices.  Bonded devices may or may not be connected.  This
     * method was refactored from <strong>list</strong> as there was a bunch of confusion with bonded
     * and connected devices.
     *
     * @param promise resolves the list of bonded devices.
     */
    @ReactMethod
    public void getBondedDevices(Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else {
            WritableArray bonded = Arguments.createArray();
            for (BluetoothDevice device : mAdapter.getBondedDevices()) {
                NativeDevice nativeDevice = new NativeDevice(device);
                bonded.pushMap(nativeDevice.map());
            }

            promise.resolve(bonded);
        }
    }

    /**
     * Lists all the currently connected devices.  Provides a {@link WritableArray}
     * of {@link BluetoothDevice}(s) which currently have an active/open connection.  Please note
     * that this does NOT list PAIRED devices, only those that are actually connected!
     *
     * @param promise resolves with the currently connected devices, may be empty.
     */
    @ReactMethod
    public void getConnectedDevices(Promise promise) {
        WritableArray connected = Arguments.createArray();
        for (DeviceConnection connection: mConnections.values()) {
            connected.pushMap(connection.getDevice().map());
        }

        if (BuildConfig.DEBUG)
            Log.d(TAG, "getConnectedDevices: " + connected.toString());

        promise.resolve(connected);
    }

    /**
     * Registers a {@link DiscoveryReceiver} and starts discovery.
     *
     * @param promise resolve or reject the request to discoverDevices
     */
    @ReactMethod
    public void startDiscovery(final Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (mDiscoveryReceiver != null) {
            promise.reject(Exceptions.BLUETOOTH_IN_DISCOVERY.name(),
                    Exceptions.BLUETOOTH_IN_DISCOVERY.message());
        } else {
            mDiscoveryReceiver = new DiscoveryReceiver(new DiscoveryReceiver.DiscoveryCallback() {
                @Override
                public void onDeviceDiscovered(NativeDevice device) {
                    Log.d(TAG, String.format("Discovered device %s", device.getAddress()));
                }

                @Override
                public void onDiscoveryFinished(Collection<NativeDevice> devices) {
                    WritableArray array = Arguments.createArray();
                    for (NativeDevice device : devices) {
                        array.pushMap(device.map());
                    }

                    promise.resolve(array);
                    mDiscoveryReceiver = null;
                }

                @Override
                public void onDiscoveryFailed(Throwable e) {
                    promise.reject(Exceptions.DISCOVERY_FAILED.name(),
                            Exceptions.DISCOVERY_FAILED.message(e.getMessage()));
                    mDiscoveryReceiver = null;
                }
            });

            getReactApplicationContext().registerReceiver(mDiscoveryReceiver,
                    DiscoveryReceiver.intentFilter());

            mAdapter.startDiscovery();
        }
    }

    /**
     * Attempts to cancel the discovery process.  Cancel request is always resolved as true at this
     * point, which may need to be changed, but for now whether anything happens or not it's
     * seen as successful.
     * <p>
     * Note - the Discovery promise will be resolved with any devices that were found during the
     * discovery period, so effectively cancelling resolves two promises.
     *
     * @param promise resolves cancel request
     */
    @ReactMethod
    public void cancelDiscovery(final Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else {
            promise.resolve(mAdapter.cancelDiscovery());
        }
    }

    /**
     * Attempts to pair/bond with the device specified by address, {@link BluetoothDevice#createBond()}
     * is only available after SDK v19.
     *
     * @param address the address of the BluetoothDevice to which we attempt pairing
     * @param promise resolves when the BluetoothDevice is paired, rejects if the SDK version is
     *                less than 19, Bluetooth is not enabled, or an Exception occurs.
     */
    @ReactMethod
    public void pairDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (Build.VERSION.SDK_INT < 19) {
            promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                    Exceptions.BONDING_UNAVAILABLE_API.message());
        } else {
            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("Attempting to pair with device %s", address));

            final PairingReceiver pr = new PairingReceiver(getReactApplicationContext(),
                new PairingReceiver.PairingCallback() {
                    @Override
                    public void onPairingSuccess(NativeDevice device) {
                        promise.resolve(device.map());
                    }

                    @Override
                    public void onPairingFailure(Exception cause) {
                        promise.reject(new DevicePairingException(null, cause));
                    }
                });
                getReactApplicationContext().registerReceiver(pr, PairingReceiver.intentFilter());
            try {
                BluetoothDevice device = mAdapter.getRemoteDevice(address);
                Method m = device.getClass().getMethod("createBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
            } catch (IllegalAccessException e) {
                getReactApplicationContext().unregisterReceiver(pr);
                promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                        Exceptions.BONDING_UNAVAILABLE_API.message());
            } catch (InvocationTargetException e) {
                getReactApplicationContext().unregisterReceiver(pr);
                promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                        Exceptions.BONDING_UNAVAILABLE_API.message());
            } catch (NoSuchMethodException e) {
                getReactApplicationContext().unregisterReceiver(pr);
                promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                        Exceptions.BONDING_UNAVAILABLE_API.message());
            }
        }
    }

    /**
     * Request that a device be unpaired.  The device Id is required - looked up using the
     * BluetoothAdapter and unpaired.
     *
     * @param address the address of the BluetoothDevice to which we attempt pairing
     * @param promise resolves when the BluetoothDevice is paired, rejects if there are any issues
     */
    @ReactMethod
    public void unpairDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (Build.VERSION.SDK_INT >= 19) {
            promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                    Exceptions.BONDING_UNAVAILABLE_API.message());
        } else {
            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("Attempting to pair with device %s", address));

            try {
                BluetoothDevice device = mAdapter.getRemoteDevice(address);
                Method m = device.getClass().getMethod("createBond", (Class[]) null);
                m.invoke(device, (Object[]) null);

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

                PairingReceiver pr = new PairingReceiver(getReactApplicationContext(),
                        new PairingReceiver.PairingCallback() {
                            @Override
                            public void onPairingSuccess(NativeDevice device) {
                                promise.resolve(device.map());
                            }

                            @Override
                            public void onPairingFailure(Exception cause) {
                                promise.reject(new DevicePairingException(new NativeDevice(device), cause));
                            }
                        });

                getReactApplicationContext().registerReceiver(pr, intentFilter);
            } catch (IllegalAccessException e) {
                promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                        Exceptions.BONDING_UNAVAILABLE_API.message());
            } catch (InvocationTargetException e) {
                promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                        Exceptions.BONDING_UNAVAILABLE_API.message());
            } catch (NoSuchMethodException e) {
                promise.reject(Exceptions.BONDING_UNAVAILABLE_API.name(),
                        Exceptions.BONDING_UNAVAILABLE_API.message());
            }
        }
    }

    /**
     * Starts accepting connections using the configured {@link ConnectionType#SERVER} device
     * connection.
     *
     * @param promise resolve or reject the requested listening
     */
    @ReactMethod
    public void accept(ReadableMap parameters, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (mConnecting.containsKey(getLocalAddress())) {
            promise.reject(Exceptions.BLUETOOTH_IN_ACCEPTING.name(),
                    Exceptions.BLUETOOTH_IN_ACCEPTING.message());
        } else {
            Properties properties = RNUtils.mapToProperties(parameters);
            String type = properties.containsKey(ConnectionProperty.TYPE.name())
                    ? properties.getProperty(ConnectionProperty.TYPE.name())
                    : ConnectionType.SERVER.name();

            DeviceConnectionFactory factory = mFactories.get(type);
            DeviceConnection connection = factory.create();

            mConnecting.put(getLocalAddress(), new DeviceConnectionPromise(connection,promise));
            connection.connect(null, properties, this);
        }
    }

    /**
     * Cancel the listening request.  Returns gracefully with a null {@link BluetoothDevice} to be
     * managed, once the error handling gets a little bit of love, there will be a difference
     * between a BluetoothAcceptCancelException and an actual error.
     *
     * @param promise the {@link Promise} resolved/rejected based on cancel success
     */
    @ReactMethod
    public void cancelAccept(Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (!mConnecting.containsKey(getLocalAddress())) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ACCEPTING.name(),
                    Exceptions.BLUETOOTH_NOT_ACCEPTING.message());
        } else {
            DeviceConnectionPromise connect = mConnecting.remove(getLocalAddress());
            connect.connection.disconnect();
            connect.promise.reject(Exceptions.ACCEPTING_CANCELLED.name(),
                    Exceptions.ACCEPTING_CANCELLED.message(getLocalAddress()));
            promise.resolve(true);
        }
    }

    /**
     * Attempts to connect to the device with the provided Id.  While the connection request is
     * active the Cancellable request will be found in the connecting map; once completed the
     * connection will be moved to the connections map.
     * <p>
     * The default {@link ConnectionType#CLIENT} will be used.  If you've provided
     * customized {@link DeviceConnection}(s) then it'll be used.
     *
     * @param address the address to which we want to connect
     * @param parameters the parameters controlling the type of connection to make
     * @param promise resolve or reject the requested connection
     */
    @ReactMethod
    public void connectToDevice(String address, ReadableMap parameters, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (mConnecting.containsKey(address)) {
            promise.reject(Exceptions.ALREADY_CONNECTING.name(),
                    Exceptions.ALREADY_CONNECTING.message(address));
        } else if (mConnections.containsKey(address)) {
            promise.reject(Exceptions.ALREADY_CONNECTED.name(),
                    Exceptions.ALREADY_CONNECTED.message(address));
        } else {
            BluetoothDevice device = mAdapter.getRemoteDevice(address);
            Properties properties = RNUtils.mapToProperties(parameters);
            String type = properties.containsKey(ConnectionProperty.TYPE.name())
                    ? properties.getProperty(ConnectionProperty.TYPE.name())
                    : ConnectionType.CLIENT.name();


            DeviceConnectionFactory factory = mFactories.get(type);
            DeviceConnection connection = factory.create();

            mConnecting.put(device.getAddress(), new DeviceConnectionPromise(connection, promise));
            connection.connect(new NativeDevice(device), properties, this);
        }
    }

    /**
     * Disconnect the BluetoothService from the currently connected device.
     *
     * @param address address of the device from which we disconnect
     * @param promise resolve or reject the disconnect request
     */
    @ReactMethod
    public void disconnectFromDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (!mConnections.containsKey(address)) {
            promise.reject(Exceptions.NOT_CURRENTLY_CONNECTED.name(),
                    Exceptions.NOT_CURRENTLY_CONNECTED.message(address));
        } else {
            DeviceConnection connection = mConnections.remove(address);
            connection.disconnect();

            promise.resolve(true);
        }
    }

    /**
     * Check to see whether the requested device has a currently established connection.  Note that
     * this is NOT paired, the connection is specific to an RFCOMM socket being open.
     *
     * @param address the address of the device which is being queried
     * @param promise resolved with the connected status
     */
    @ReactMethod
    public void isDeviceConnected(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else {
            promise.resolve(mConnections.containsKey(address));
        }
    }

    /**
     * Attempt to get the connection representing the device address requested.
     *
     * @param address the address of the device which is being queried
     * @param promise resolved with the connected status
     */
    @ReactMethod
    public void getConnectedDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else {
            if (!mConnections.containsKey(address)) {
                promise.reject(new BluetoothException(address + " is not currently connected"));
            } else {
                DeviceConnection connection = mConnections.get(address);
                promise.resolve(connection.getDevice().map());
            }
        }
    }


    /**
     * Attempts to write to the device.  I'm not sure if there is a better way to do this, but all
     * communication needs to come through the module.  It would be awesome if we could
     * dynamically add a BluetoothConnectionModuleXXXX to the Application in order to allow
     * each device to be it's own module for communication.
     *
     * @param address address of the device to which we will write the data
     * @param message base64 encoded message to be sent
     * @param promise resolved once the message has been written.
     */
    @ReactMethod
    public void writeToDevice(String address, String message, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (!mConnections.containsKey(address)) {
            promise.reject(Exceptions.NOT_CURRENTLY_CONNECTED.name(),
                    Exceptions.NOT_CURRENTLY_CONNECTED.message(address));
        } else {
            // At this point the data should be encoded correctly.  The original version did some
            // weird decoding and encoding but it didn't seem right.  At this point leaving it
            // as is and required to be set at the client level.
            byte[] data = Base64.decode(message, Base64.DEFAULT);

            try {
                mConnections.get(address).write(data);
                promise.resolve(true);
            } catch (IOException e) {
                promise.reject(Exceptions.WRITE_FAILED.name(),
                        Exceptions.WRITE_FAILED.message(e.getMessage()));
            }
        }
    }

    /**
     * Attempts to read from the device.  The full buffer is read (then cleared) without using the
     * mDelimiter.  Note - there will never be data within the buffer if the application is currently
     * registered to receive read events.
     *
     * @param address device address to which we wish to read
     * @param promise resolves with data, could be null or 0 length
     */
    @ReactMethod
    public void readFromDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (!mConnections.containsKey(address)) {
            promise.reject(Exceptions.NOT_CURRENTLY_CONNECTED.name(),
                    Exceptions.NOT_CURRENTLY_CONNECTED.message(address));
        } else {
            try {
                String message = mConnections.get(address).read();
                promise.resolve(message);
            } catch (IOException e) {
                promise.reject(Exceptions.READ_FAILED.name(),
                        Exceptions.READ_FAILED.message(e.getMessage()));
            }
        }
    }

    /**
     * Clears the buffer.
     *
     * @param address the address of the device whose buffer is to be cleared
     * @param promise resolves true
     */
    @ReactMethod
    public void clearFromDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (!mConnections.containsKey(address)) {
            promise.reject(Exceptions.NOT_CURRENTLY_CONNECTED.name(),
                    Exceptions.NOT_CURRENTLY_CONNECTED.message(address));
        } else {
            mConnections.get(address).clear();
            promise.resolve(true);
        }
    }

    /**
     * Gets the available information within the buffer.  There is no concept of the delimiter in
     * this request - which may need to be changed - since in most cases I can see a full message
     * needing to be available.
     *
     * @param address device address for which the client wishes to read
     * @param promise resolves with length of buffer, could be 0
     */
    @ReactMethod
    public void availableFromDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (!mConnections.containsKey(address)) {
            promise.reject(Exceptions.NOT_CURRENTLY_CONNECTED.name(),
                    Exceptions.NOT_CURRENTLY_CONNECTED.message(address));
        } else {
            promise.resolve(mConnections.get(address).available());
        }
    }

    /**
     * Attempts to set the BluetoothAdapter name.
     *
     * @param newName the name to which the adapter will be changed
     * @param promise resolves true
     * @deprecated unsure if this is really required from the application.  Not a fan of having
     * extra functionality in here that may never get called and isn't available on IOS
     */
    @Deprecated
    @ReactMethod
    public void setBluetoothAdapterName(String newName, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else {
            mAdapter.setName(newName);
            promise.resolve(true);
        }
    }

    /**
     * Registers the module wide {@link BroadcastReceiver}(s).  These include:
     * <ul>
     *     <li>{@link BluetoothAdapter} state changes</li>
     *     <li>{@link BluetoothDevice} ACL action changes - these include device connections and
     *          disconnections</li>
     * </ul>
     */
    private void registerBluetoothReceivers() {
        if (mStateChangeReceiver == null) {
            mStateChangeReceiver = new StateChangeReceiver(this);
            getReactApplicationContext()
                    .registerReceiver(mStateChangeReceiver, StateChangeReceiver.intentFilter());
        }

        if (mActionACLReceiver == null) {
            mActionACLReceiver = new ActionACLReceiver(this);
            getReactApplicationContext()
                    .registerReceiver(mActionACLReceiver, ActionACLReceiver.intentFilter());
        }
    }

    /**
     * Unregister receivers.  "Global" receivers need to always be removed, they are not configured
     * to be removed on their own.
     */
    private void unregisterBluetoothReceivers() {
        if (mStateChangeReceiver != null) {
            getReactApplicationContext().unregisterReceiver(mStateChangeReceiver);
            mStateChangeReceiver = null;
        }

        if (mActionACLReceiver != null) {
            getReactApplicationContext().unregisterReceiver(mActionACLReceiver);
            mActionACLReceiver = null;
        }

        if (mDiscoveryReceiver != null) {
            mAdapter.cancelDiscovery();
            getReactApplicationContext().unregisterReceiver(mDiscoveryReceiver);
            mDiscoveryReceiver = null;
        }
    }

    /**
     * Adds a new listener for the {@link BluetoothEvent} provided.  If a {@link BluetoothEvent#DEVICE_READ}
     * is requested, the device address must accompany it (separated by an @).   If an unsupported
     * {@link BluetoothEvent} is requested, and error is thrown.
     *
     * @param requestedEvent {@link BluetoothEvent} name for which the client wishes to listen
     */
    @ReactMethod
    public void addListener(String requestedEvent) {
        String[] eventSplit = requestedEvent.split("@");
        String eventType = eventSplit[0];

        if (!BluetoothEvent.eventNames().hasKey(eventSplit[0])) {
            throw new InvalidBluetoothEventException(requestedEvent);
        }

        BluetoothEvent event = BluetoothEvent.valueOf(eventSplit[0]);

        if (BluetoothEvent.DEVICE_READ == event) {
            String address = eventSplit[1];

            if (!mConnections.containsKey(address)) {
                throw new IllegalStateException(String.format("Cannot read from %s, not currently connected", requestedEvent));
            }

            DeviceConnection connection = mConnections.get(address);
            connection.addDeviceListener(this);
        }

        // Now we can increment the listener as appropriate
        AtomicInteger listenerCount = mListenerCounts.get(requestedEvent);
        if (listenerCount == null) {
            listenerCount = new AtomicInteger(0);
            if (mListenerCounts.containsKey(requestedEvent))
                mListenerCounts.put(requestedEvent, listenerCount);
        }
        int currentCount = listenerCount.incrementAndGet();

        Log.d(TAG, String.format("Adding listener to %s, currently have %d listeners",
                requestedEvent, currentCount));
    }

    /**
     * Removes the specified {@link BluetoothEvent}.  If this is a {@link BluetoothEvent#DEVICE_READ}
     * the device address must be supplied (separated by an @) in the same way as when the
     * listener was applied.
     *
     * @param eventName name of the {@link BluetoothEvent} for which the client wishes to remove
     *                  listener.
     */
    @ReactMethod
    public void removeListener(String eventName) {
        if (!BluetoothEvent.eventNames().hasKey(eventName)) {
            throw new InvalidBluetoothEventException(eventName);
        }

        String[] requestedEvent = eventName.split("@");
        BluetoothEvent event = BluetoothEvent.valueOf(requestedEvent[0]);

        if (BluetoothEvent.DEVICE_READ == event) {
            if (!mConnections.containsKey(requestedEvent[1])) {
                throw new IllegalStateException(String.format("Cannot read from %s, not currently connected", eventName));
            }

            DeviceConnection connection = mConnections.get(requestedEvent[1]);
            connection.removeDeviceListener();
        }

        // Only remove the listener if it currently exists.  If you're attemping to remove a listener
        // which hasn't been added, just let it go.
        if (mListenerCounts.containsKey(eventName)) {
            AtomicInteger listenerCount = mListenerCounts.get(eventName);
            int currentCount = listenerCount.decrementAndGet();

            Log.d(TAG,
                    String.format("Removing listener to %s, currently have %d listeners",
                            eventName, currentCount));
        }
    }

    /**
     * Remove all the listeners for the provided eventName.
     *
     * @param eventName for which all listeners will be removed
     */
    @ReactMethod
    public void removeAllListeners(String eventName) {
        if (!BluetoothEvent.eventNames().hasKey(eventName)) {
            throw new InvalidBluetoothEventException(eventName);
        }

        String[] requestedEvent = eventName.split("@");
        BluetoothEvent event = BluetoothEvent.valueOf(requestedEvent[0]);

        if (BluetoothEvent.DEVICE_READ == event) {
            if (!mConnections.containsKey(requestedEvent[1])) {
                throw new IllegalStateException(String.format("Cannot read from %s, not currently connected", eventName));
            }

            DeviceConnection connection = mConnections.get(requestedEvent[1]);
            connection.removeDeviceListener();
        }

        // Only remove the listener if it currently exists.  If you're attemping to remove a listener
        // which hasn't been added, just let it go.
        if (mListenerCounts.containsKey(eventName)) {
            AtomicInteger listenerCount = mListenerCounts.get(eventName);
            listenerCount.set(0);

            Log.d(TAG,
                    String.format("Removing listener to %s, currently have %d listeners",
                            eventName, 0));
        }
    }
    //endregion

    //region: DeviceConnectionListener

    /**
     * Called when a connection is established, prior to attempting to read data.   This will
     * resolve the Connection promise and send a DEVICE_CONNECTED event.
     * <p>
     * Another one that may not need to be done through an event, when the Promise is the real key.
     * Although this still provides a way for managing connections without worrying about
     * promises.
     *
     * @param device
     */
    @Override
    public void onConnectionSuccess(NativeDevice device) {
        DeviceConnectionPromise connection = mConnecting.remove(device.getAddress());
        mConnections.put(device.getAddress(), connection.getConnection());
        connection.resolve();

        sendEvent(BluetoothEvent.DEVICE_CONNECTED, device.map());
    }

    /**
     * Connection failure occurs if there is a problem while attempting to establish the connection.
     * It shouldn't be called after the device is actually connected.  There is no event thrown
     * for a failed connection, it's just the Promise that will be rejected.
     *
     * @param device the device which was attempting connection
     * @param e the Exception stopping connection
     */
    @Override
    public void onConnectionFailure(NativeDevice device, Throwable e) {
        DeviceConnectionPromise connection = mConnecting.remove(device.getAddress());

        if (connection != null)
            connection.promise.reject(Exceptions.CONNECTION_FAILED.name(),
                    Exceptions.CONNECTION_FAILED.message(device.getAddress()),
                    new ConnectionFailedException(device, e).map());

        mConnections.remove(device.getAddress());
    }

    /**
     * Called from the {@link DeviceConnectionListener} when a device ConnectedThread fails or
     * closed.  This will double the DEVICE_DISCONNECTED event, which I don't think is a good thing
     * but it's how the system works now.
     * <p>
     * Need to decide which method for publishing the DEVICE_DISCONNECTED event is more practical
     * and go with that.
     *
     * @param device the device which was disconnected
     * @param e the Exception which was thrown to cause the disconnection
     */
    @Override
    public void onConnectionLost(NativeDevice device, Throwable e) {
        ConnectionLostException ex = new ConnectionLostException(device, e);

        DeviceConnectionPromise connection = mConnecting.remove(device.getAddress());
        if (connection != null)
            connection.promise.reject(Exceptions.CONNECTION_LOST.name(),
                    Exceptions.CONNECTION_LOST.message(device.getAddress()),
                    ex.map());

        mConnections.remove(device.getAddress());
        sendEvent(BluetoothEvent.DEVICE_DISCONNECTED, ex.map());
    }

    /**
     * General error, this should NOT be used for disconnections.
     *
     * @param device the device on which the error occurred
     * @param e the Exception which was thrown
     */
    @Override
    public void onError(NativeDevice device, Throwable e) {
        BluetoothException ex = new BluetoothException(device, e.getMessage(), e);
        sendEvent(BluetoothEvent.ERROR, ex.map());
    }
    //endregion

    //region: DataReceivedListener
    /**
     * Handles incoming data from the device.  At this point the data should have been passed through
     * any appropriate transformations and is now in a format that React JS can parse correctly.
     * This means that any ASCII, UTF-8, hex or binary data is already converted to Strings.
     * <p>
     * Previously we checked whether the {@code readObserving} flag was set.  But now that we allow
     * for multiple connections as soon as a READ listener is applied, we know that this device has
     * specified the data.
     *
     * @param device the {@link NativeDevice} which received data
     * @param data the data which was received.  At this point the data should be in string format
     *             waiting to be accepted by Javascript.  It should have been parsed and encoded
     *             by the {@link DeviceConnection}.
     */
    @Override
    public void onDataReceived(NativeDevice device, String data) {
        Log.d(TAG, String.format("Received translated data from the device: %s", data));

        BluetoothMessage bluetoothMessage
                = new BluetoothMessage<>(device.map(), data);
        sendEvent(BluetoothEvent.DEVICE_READ, device, bluetoothMessage.asMap());
    }

    /**
     * Called from the {@link StateChangeReceiver} when the {@link BluetoothAdapter} state
     * is changed.  Fires the appropriate event to any listeners.
     *
     * @param newState the new {@link BluetoothState}
     * @param oldState the previous {@link BluetoothState}
     */
    @Deprecated
    @Override
    public void onStateChange(BluetoothState newState, BluetoothState oldState) {
        Log.d(TAG, "onStateChange from " + oldState.name() + "  to " + newState.name());

        BluetoothEvent event = (BluetoothState.ENABLED == newState)
                ? BluetoothEvent.BLUETOOTH_ENABLED : BluetoothEvent.BLUETOOTH_DISABLED;

        sendEvent(event, new BluetoothStateChangeEvent(newState).map());
    }

    /**
     * Not sure whether this provides any actual info.
     *
     * @param device the {@link NativeDevice} which just requested disconnect
     *
     * @deprecated may be removed at some point unless it is found useful (opposed to ACLDisconnected)
     */
    @Deprecated
    @Override
    public void onACLDisconnectRequest(NativeDevice device) {
        Log.d(TAG, "onACLDisconnectRequest to " + device.getAddress());
    }

    /**
     * Called when we get an ACL level device disconnection.  This happens when a socket is closed,
     * for whatever reason.  If there is a currently active connection (which there should be)
     * it's disconnected, then a DEVICE_DISCONNECTED event is fired.
     * <p>
     * This also sends an event to the {@code DEVICE_DISCONNECTED@address} making the Device more
     * responsible for it's own connectivity.
     *
     * @param device the {@link NativeDevice} which was just disconnected
     */
    @Override
    public void onACLDisconnected(NativeDevice device) {
        Log.d(TAG, "onACLDisconnected to " + device.getAddress());

        DeviceConnection connection = mConnections.remove(device.getAddress());

        sendEvent(BluetoothEvent.DEVICE_DISCONNECTED, device.map());
        sendEvent(BluetoothEvent.DEVICE_DISCONNECTED, device, Arguments.createMap());
    }
    //endregion

    /**
     * Sends a {@link BluetoothEvent} to the React Native JS module
     * {@link com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter}.
     * <p>
     * Currently having no active {@link com.facebook.react.bridge.CatalystInstance} will not cause
     * the application to crash, although I'm not sure if it should.
     *
     * @param event the {@link BluetoothEvent} being sent
     * @param body the content of the event
     */
    private void sendEvent(BluetoothEvent event, WritableMap body) {
        ReactContext context = getReactApplicationContext();

        if (context.hasActiveCatalystInstance()) {
            context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(event.name(), body);
        } else {
            Log.e(TAG, "There is currently no active Catalyst instance");
        }
    }

    /**
     * Sends a {@link BluetoothEvent} to the React Native JS module
     * {@link com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter}.  This
     * version allows for sending events do a specific Device (providing the address along side
     * the event name separated by a semi-colon).
     * <p>
     * This should generally only be used for {@link BluetoothEvent#DEVICE_READ} events, but nothing
     * stops it from providing other types.
     *
     * @param event the {@link BluetoothEvent} being sent to React Native JS
     * @param device the {@link NativeDevice} which caused/receiving the event
     * @param body the event content
     */
    private void sendEvent(BluetoothEvent event, NativeDevice device, WritableMap body) {
        ReactContext context = getReactApplicationContext();

        if (context.hasActiveCatalystInstance()) {
            context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(String.format("%s@%s", event.name(), device.getAddress()), body);
        } else {
            Log.e(TAG, "There is currently no active Catalyst instance");
        }
    }

    /**
     * Manages a {@link DeviceConnection} and it's {@link Promise}.
     *
     * @author kendavidson
     */
    static class DeviceConnectionPromise {
        private DeviceConnection connection;
        private Promise promise;

        DeviceConnectionPromise(DeviceConnection connection,
                                       Promise promise) {
            this.connection = connection;
            this.promise = promise;
        }

        DeviceConnection getConnection() {
            return connection;
        }

        void resolve() {
            promise.resolve(connection.getDevice().map());
        }
    }
}
