
package kjd.reactnative.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import kjd.reactnative.android.BiConsumer;
import kjd.reactnative.bluetooth.conn.AcceptFailedException;
import kjd.reactnative.bluetooth.conn.ConnectionAcceptor;
import kjd.reactnative.bluetooth.conn.ConnectionAcceptorFactory;
import kjd.reactnative.bluetooth.conn.ConnectionConnector;
import kjd.reactnative.bluetooth.conn.ConnectionConnectorFactory;
import kjd.reactnative.bluetooth.conn.ConnectionFailedException;
import kjd.reactnative.bluetooth.conn.StandardOption;
import kjd.reactnative.bluetooth.event.BluetoothStateEvent;
import kjd.reactnative.bluetooth.event.EventType;
import kjd.reactnative.bluetooth.conn.DeviceConnection;
import kjd.reactnative.bluetooth.conn.DeviceConnectionFactory;
import kjd.reactnative.bluetooth.device.NativeDevice;
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
@SuppressWarnings({"WeakerAccess"})
public class RNBluetoothClassicModule
        extends ReactContextBaseJavaModule
        implements ActivityEventListener,
        LifecycleEventListener,
        StateChangeReceiver.StateChangeCallback,
        ActionACLReceiver.ActionACLCallback {

    /**
     * Name of the module when provided to React Native {@code NativeModules}.
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
     * Provides {@link ConnectionAcceptorFactory}(s) to {@link #accept} method.
     */
    private final Map<String, ConnectionAcceptorFactory> mAcceptorFactories;

    /**
     * Provides a map of all available {@link ConnectionConnectorFactory} available to the
     * {@link #connectToDevice} method.   A {@link ConnectionConnector} is first started, then upon
     * completion the {@link BluetoothSocket} is passed into the requested {@link DeviceConnection}.
     */
    private final Map<String, ConnectionConnectorFactory> mConnectorFactories;

    /**
     * Provides a map of all the available {@link DeviceConnectionFactory} available to the
     * {@link #connectToDevice} method.
     */
    private final Map<String,DeviceConnectionFactory> mConnectionFactories;

    /**
     * Manages {@link DeviceConnection} wrapping {@link BluetoothDevice} by
     * {@link BluetoothDevice#getAddress()}.  Currently the initial capacity is 1, since the main
     * goal of this was a simple connection.  This may need to be updated to have the default
     * size updated during package creation.
     */
    private Map<String, DeviceConnection> mConnections;

    /**
     * Maintains a map of {@link ConnectionConnector}(s) keyed on {@link BluetoothDevice} address.
     * Connectors are added during the {@link #connectToDevice} request and removed when either
     * successful or failed.
     */
    private Map<String, ConnectionConnector> mConnecting;

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
     * Promise must be maintained across Activity requests for managing the enabled request
     * status.
     */
    private Promise mEnabledPromise;

    /**
     * Manage the number of listeners of a specific type - these event types are that of
     * the bluetooth mAdapter in general (connect, disconnect, etc.) and not those which are
     * reading.  Those are managed separately within the device itself.
     */
    private Map<String, AtomicInteger> mListenerCounts;

    /**
     * Maintains the {@link ConnectionAcceptor} when the module has been placed into accept
     * mode.   Only one type of {@link ConnectionAcceptor} is allowed at one time, regardless
     * of how many are configured.  Current accepting should be cancelled and restarted in order
     * to change the type.
     */
    private ConnectionAcceptor mAcceptor;

    //region: Constructors

    /**
     * Creates the RNBlutoothClassicModule.  As a final step of initialization the appropriate
     * {@link EventType#BLUETOOTH_ENABLED}/{@link EventType#BLUETOOTH_DISABLED} is
     * sent and the activity and lifecyle listeners are register.
     *
     * @param context React application context
     * @param factories {@link DeviceConnection} factories
     */
    public RNBluetoothClassicModule(ReactApplicationContext context,
                                    Map<String,ConnectionAcceptorFactory> acceptFactories,
                                    Map<String,ConnectionConnectorFactory> connectFactories,
                                    Map<String,DeviceConnectionFactory> factories) {
        super(context);

        this.mAdapter = BluetoothAdapter.getDefaultAdapter();

        this.mAcceptorFactories = Collections.unmodifiableMap(acceptFactories);
        this.mConnectorFactories = Collections.unmodifiableMap(connectFactories);
        this.mConnectionFactories = Collections.unmodifiableMap(factories);

        this.mConnections = new ConcurrentHashMap<>(1);
        this.mConnecting = new ConcurrentHashMap<>(1);
        this.mListenerCounts = new ConcurrentHashMap<>();

        if (mAdapter != null && mAdapter.isEnabled()) {
            sendEvent(EventType.BLUETOOTH_ENABLED,
                    new BluetoothStateEvent(BluetoothState.ENABLED).map());
        } else {
            sendEvent(EventType.BLUETOOTH_DISABLED,
                    new BluetoothStateEvent(BluetoothState.DISABLED).map());
        }

        getReactApplicationContext().addActivityEventListener(this);
        getReactApplicationContext().addLifecycleEventListener(this);
    }
    //endregion

    //region: ReactContextBaseJavaModule methods
    @Override
    @NonNull
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public boolean hasConstants() {
        return true;
    }

    /**
     * Previously this returned the Bluetooth events and common character sets that were available
     * on the Android system.  This wasn't testable, and I've decided to open it up to make things
     * more generic, and let users handle their own issues.
     *
     * @return constants provided by
     *
     */
    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.emptyMap();
    }
    //endregion

    //region: Helper/Utility Methods
    private boolean checkBluetoothAdapter() {
        return (mAdapter != null && mAdapter.isEnabled());
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
     * This sends a {@link EventType#BLUETOOTH_ENABLED} event.  It probably shouldn't duplicate
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
                    sendEvent(EventType.BLUETOOTH_ENABLED,
                            new BluetoothStateEvent(BluetoothState.ENABLED).map());
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
     * Requests that the Android Bluetooth mAdapter be enabled.  If the mAdapter is already enabled
     * then the promise is returned true.  If the mAdapter is not enabled, an Intent request is sent
     * to Android (promised saved for use upon result).
     * <p>
     * Note that this does not inherently fire a state change event, as the manual act seems to
     * skip the StateChangeReceiver functionality.
     *
     * @param promise resolves <strong>true</strong> if Bluetooth is already enabled or when
     *                Bluetooth becomes enabled.  Rejects if anything else
     */
    @ReactMethod
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public void getConnectedDevices(Promise promise) {
        WritableArray connected = Arguments.createArray();
        for (DeviceConnection connection: mConnections.values()) {
            connected.pushMap(new NativeDevice(connection.getDevice()).map());
        }

        Log.d(TAG, "getConnectedDevices: " + connected.toString());

        promise.resolve(connected);
    }

    /**
     * Registers a {@link DiscoveryReceiver} and starts discovery.
     *
     * @param promise resolve or reject the request to discoverDevices
     */
    @ReactMethod
    @SuppressWarnings("unused")
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
                    // This wasn't previously an event, but now we can send out and request them
                    Log.d(TAG, String.format("Discovered device %s", device.getAddress()));
                    sendEvent(EventType.DEVICE_DISCOVERED, device.map());
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
     * Puts the {@link BluetoothAdapter} into an accept mode using the provided accept type
     * configured on the module.
     *
     * @param promise resolve or reject the requested listening
     */
    @ReactMethod
    @SuppressWarnings("unused")
    public void accept(ReadableMap parameters, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (mAcceptor != null) {
            promise.reject(Exceptions.BLUETOOTH_IN_ACCEPTING.name(),
                    Exceptions.BLUETOOTH_IN_ACCEPTING.message());
        } else {
            Properties properties = Utilities.mapToProperties(parameters);

            try {
                String connectorType = StandardOption.ACCEPTOR_TYPE.get(properties);
                if (!mAcceptorFactories.containsKey(connectorType))
                    throw new IllegalStateException(
                            String.format("No ConnectionAcceptorFactory configured for type %s", connectorType));

                ConnectionAcceptorFactory acceptorFactory = mAcceptorFactories.get(connectorType);
                ConnectionAcceptor acceptor = acceptorFactory.create(mAdapter, properties);
                acceptor.addListener(new ConnectionAcceptor.AcceptorListener<BluetoothSocket>() {
                    @Override
                    public void success(BluetoothSocket bluetoothSocket) {
                        BluetoothDevice device = bluetoothSocket.getRemoteDevice();
                        NativeDevice nativeDevice = new NativeDevice(device);

                        try {
                            // Create the appropriate Connection type and add it to the connected list

                            String connectionType = StandardOption.CONNECTION_TYPE.get(properties);
                            DeviceConnectionFactory connectionFactory = mConnectionFactories.get(connectionType);
                            DeviceConnection connection = connectionFactory.create(bluetoothSocket, properties);
                            connection.onDisconnect(onDisconnect);
                            mConnections.put(device.getAddress(), connection);

                            // Now start the connection and let React Native know
                            Thread ct = new Thread(connection);
                            ct.start();

                            promise.resolve(nativeDevice.map());
                        } catch (IOException e) {
                            promise.reject(new ConnectionFailedException(nativeDevice, e));
                        }
                    }

                    @Override
                    public void failure(Exception e) {
                        promise.reject(new AcceptFailedException(e.getMessage(), e));
                    }
                });

                this.mAcceptor = acceptor;
                this.mAcceptor.start();

            } catch(IOException e) {
                promise.reject(new AcceptFailedException(e.getMessage(), e));
            } catch (IllegalStateException e) {
                promise.reject(e);
            }
        }
    }

    /**
     * Attempts to cancel the Accepting thread.
     * <p>
     * If the {@link BluetoothAdapter} is unavailable then the promise will be rejected with a
     * Bluetooth not enabled message.
     * <p>
     * Otherwise the promise will be resolved {@code true}.  This was changed as previously an
     * reject would also occur if the Device was not in accept mode.  This has been changed as it
     * made more sense to just let React Native app set {@code accepting: false} (kind of like a
     * status check without {@code isAccepting} being required.
     *
     * @param promise the {@link Promise} resolved/rejected based on cancel success
     */
    @ReactMethod
    @SuppressWarnings("unused")
    public void cancelAccept(Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else {
            if (mAcceptor != null) {
                mAcceptor.cancel();
            }

            mAcceptor = null;

            promise.resolve(true);
        }
    }

    /**
     * Attempts to connect to the device with the provided Id.  While the connection request is
     * active the Cancellable request will be found in the connecting map; once completed the
     * connection will be moved to the connections map.
     * <p>
     * The default client connection type will be used.  If you've provided
     * customized {@link DeviceConnection}(s) then it'll be used.
     *
     * @param address the address to which we want to connect
     * @param parameters the parameters controlling the type of connection to make
     * @param promise resolve or reject the requested connection
     */
    @ReactMethod
    @SuppressWarnings("unused")
    public void connectToDevice(String address, ReadableMap parameters, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (mConnecting.containsKey(address)) {
            promise.reject(Exceptions.ALREADY_CONNECTING.name(),
                    Exceptions.ALREADY_CONNECTING.message(address));
        } else if (mConnections.containsKey(address)) {
            // If it's already connected just return the device now.
            DeviceConnection connection = mConnections.get(address);
            promise.resolve(new NativeDevice(connection.getDevice()).map());
        } else {
            final BluetoothDevice device = mAdapter.getRemoteDevice(address);
            final NativeDevice nativeDevice = new NativeDevice(device);

            try {
                // Issue/84 just in case the React Native side gets circumvented somehow
                // this matches the IOS side of a new parameters being added to a NSDictionary
                Properties properties = parameters == null
                        ? new Properties() : Utilities.mapToProperties(parameters);

                final String connectorType = StandardOption.CONNECTOR_TYPE.get(properties);
                if (!mConnectorFactories.containsKey(connectorType)) {
                    promise.reject(Exceptions.INVALID_CONNECTOR_TYPE.name(),
                            Exceptions.INVALID_CONNECTOR_TYPE.message(connectorType));
                    return;
                }

                final String connectionType = StandardOption.CONNECTION_TYPE.get(properties);
                if (!mConnectionFactories.containsKey(connectionType)) {
                    promise.reject(Exceptions.INVALID_CONNECTION_TYPE.name(),
                            Exceptions.INVALID_CONNECTION_TYPE.message(connectorType));
                    return;
                }

                ConnectionConnectorFactory connectorFactory = mConnectorFactories.get(connectorType);
                ConnectionConnector connector = connectorFactory.create(device, properties);
                connector.addListener(new ConnectionConnector.ConnectorListener<BluetoothSocket>() {
                    @Override
                    public void success(BluetoothSocket bluetoothSocket) {
                        // Remove from connecting and add to connected
                        mConnecting.remove(address);

                        try {
                            // Create the appropriate Connection type and add it to the connected list
                            DeviceConnectionFactory connectionFactory = mConnectionFactories.get(connectionType);
                            DeviceConnection connection = connectionFactory.create(bluetoothSocket, properties);
                            connection.onDisconnect(onDisconnect);
                            mConnections.put(address, connection);

                            // Now start the connection and let React Native know
                            new Thread(connection).start();
                            promise.resolve(nativeDevice.map());
                        } catch (IOException e) {
                            promise.reject(new ConnectionFailedException(nativeDevice, e));
                        }
                    }

                    @Override
                    public void failure(Exception e) {
                        // Remove from connecting and notify of failure
                        mConnecting.remove(address);
                        promise.reject(new ConnectionFailedException(nativeDevice, e));
                    }
                });

                mConnecting.put(address, connector);
                connector.start();
            } catch (IOException e) {
                promise.reject(new ConnectionFailedException(nativeDevice, e));
            } catch (IllegalStateException e) {
                promise.reject(e);
            }
        }
    }

    /**
     * Disconnect the BluetoothService from the currently connected device.
     *
     * @param address address of the device from which we disconnect
     * @param promise resolve or reject the disconnect request
     */
    @ReactMethod
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public void getConnectedDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else {
            if (!mConnections.containsKey(address)) {
                promise.reject(new BluetoothException(address + " is not currently connected"));
            } else {
                DeviceConnection connection = mConnections.get(address);
                promise.resolve(new NativeDevice(connection.getDevice()).map());
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
    @SuppressWarnings("unused")
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
     * <p>
     * Might be configurable to reject when there is no data, instead of resolve null.
     *
     * @param address device address to which we wish to read
     * @param promise resolves with data, could be null or 0 length
     */
    @ReactMethod
    @SuppressWarnings("unused")
    public void readFromDevice(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(Exceptions.BLUETOOTH_NOT_ENABLED.name(),
                    Exceptions.BLUETOOTH_NOT_ENABLED.message());
        } else if (!mConnections.containsKey(address)) {
            promise.reject(Exceptions.NOT_CURRENTLY_CONNECTED.name(),
                    Exceptions.NOT_CURRENTLY_CONNECTED.message(address));
        } else {
            String message = mConnections.get(address).read();
            promise.resolve(message);
        }
    }

    /**
     * Clears the buffer.
     *
     * @param address the address of the device whose buffer is to be cleared
     * @param promise resolves true
     */
    @ReactMethod
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
     * @param newName the name to which the mAdapter will be changed
     * @param promise resolves true
     * @deprecated unsure if this is really required from the application.  Not a fan of having
     * extra functionality in here that may never get called and isn't available on IOS
     */
    @Deprecated
    @ReactMethod
    @SuppressWarnings("unused")
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

    private BiConsumer<BluetoothDevice,Exception> onDisconnect = (BluetoothDevice device, Exception e) -> {
            Log.d(TAG, String.format("Disconnected from device %s due to %s", device.getName(), e.getMessage()));

            // At this point just remove the connection, the DEVICE_DISCONNECTED should have been
            // sent from the ACL message already.
            mConnections.remove(device.getAddress());
            sendEvent(EventType.DEVICE_DISCONNECTED, new NativeDevice(device), new BluetoothException(e.getMessage()).map());
        };

    private BiConsumer<BluetoothDevice,String> onReceivedData = (BluetoothDevice device, String data) -> {
            Log.d(TAG, String.format("Received translated data from the device: %s", data));

            NativeDevice nativeDevice = new NativeDevice(device);
            BluetoothMessage bluetoothMessage = new BluetoothMessage<>(nativeDevice.map(), data);
            sendEvent(EventType.DEVICE_READ, nativeDevice, bluetoothMessage.asMap());
        };

    /**
     * Adds a new listener for the {@link EventType} provided.
     * <p>
     * Listeners can be provided with or without a device context.  A device context is applied
     * by sending the event name followed by a device's address.  An example of this would be
     * {@code READ@12:34:56:78:90}.
     *
     * @param requestedEvent {@link EventType} name for which the client wishes to listen
     */
    @ReactMethod
    @SuppressWarnings({"unused"})
    public void addListener(String requestedEvent) {
        String eventType = requestedEvent,
                eventDevice = null;

        if (requestedEvent.contains("@")) {
            String[] context = requestedEvent.split("@");
            eventType = context[0];
            eventDevice = context[1];
        }

        if (!EventType.eventNames().hasKey(eventType)) {
            throw new InvalidBluetoothEventException(requestedEvent);
        }

        EventType event = EventType.valueOf(eventType);

        if (EventType.DEVICE_READ == event) {
            if (!mConnections.containsKey(eventDevice)) {
                throw new IllegalStateException(String.format("Cannot read from %s, not currently connected", requestedEvent));
            }

            DeviceConnection connection = mConnections.get(eventDevice);
            connection.onDataReceived(onReceivedData);
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
     * Removes the specified {@link EventType}.  If this is a {@link EventType#DEVICE_READ}
     * the device address must be supplied (separated by an @) in the same way as when the
     * listener was applied.
     *
     * @param requestedEvent name of the {@link EventType} for which the client wishes to remove
     *                  listener.
     */
    @ReactMethod
    @SuppressWarnings({"unused"})
    public void removeListener(String requestedEvent) {
        String eventType = requestedEvent,
                eventDevice = null;

        if (requestedEvent.contains("@")) {
            String[] context = requestedEvent.split("@");
            eventType = context[0];
            eventDevice = context[1];
        }

        if (!EventType.eventNames().hasKey(eventType)) {
            return;
        }

        EventType event = EventType.valueOf(eventType);

        if (EventType.DEVICE_READ == event) {
            if (!mConnections.containsKey(eventDevice)) {
                throw new IllegalStateException(String.format("Cannot read from %s, not currently connected", eventType));
            }

            DeviceConnection connection = mConnections.get(eventDevice);
            connection.clearOnDataReceived();
        }

        // Only remove the listener if it currently exists.  If you're attemping to remove a listener
        // which hasn't been added, just let it go.
        if (mListenerCounts.containsKey(eventType)) {
            AtomicInteger listenerCount = mListenerCounts.get(eventType);
            int currentCount = listenerCount.decrementAndGet();

            Log.d(TAG,
                    String.format("Removing listener to %s, currently have %d listeners",
                            eventType, currentCount));
        }
    }

    /**
     * Remove all the listeners for the provided eventName.   Removing all listeners also has a
     * context (prefixed with @) which will remove all the listeners for that specified device.
     *
     * @param requestedEvent for which all listeners will be removed
     */
    @ReactMethod
    @SuppressWarnings({"unused"})
    public void removeAllListeners(String requestedEvent) {
        String eventType = requestedEvent,
                eventDevice = null;

        if (requestedEvent.contains("@")) {
            String[] context = requestedEvent.split("@");
            eventType = context[0];
            eventDevice = context[1];
        }

        if (!EventType.eventNames().hasKey(eventType)) {
            return;
        }

        EventType event = EventType.valueOf(eventType);

        if (EventType.DEVICE_READ == event) {
            if (!mConnections.containsKey(eventDevice)) {
                throw new IllegalStateException(String.format("Cannot read from %s, not currently connected", eventType));
            }

            DeviceConnection connection = mConnections.get(eventDevice);
            connection.clearOnDataReceived();
        }

        // Only remove the listener if it currently exists.  If you're attemping to remove a listener
        // which hasn't been added, just let it go.
        if (mListenerCounts.containsKey(eventType)) {
            AtomicInteger listenerCount = mListenerCounts.get(eventType);
            listenerCount.set(0);

            Log.d(TAG,
                    String.format("Removing listener to %s, currently have %d listeners",
                            eventType, 0));
        }
    }
    //endregion

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

        EventType event = (BluetoothState.ENABLED == newState)
                ? EventType.BLUETOOTH_ENABLED : EventType.BLUETOOTH_DISABLED;

        sendEvent(event, new BluetoothStateEvent(newState).map());
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

        mConnections.remove(device.getAddress());
        sendEvent(EventType.DEVICE_DISCONNECTED, device.map());
    }

    /**
     * Sends a {@link EventType} to the React Native JS module
     * {@link com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter}.
     * <p>
     * Currently having no active {@link com.facebook.react.bridge.CatalystInstance} will not cause
     * the application to crash, although I'm not sure if it should.
     *
     * @param event the {@link EventType} being sent
     * @param body the content of the event
     */
    private void sendEvent(EventType event, WritableMap body) {
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
     * Sends a {@link EventType} to the React Native JS module
     * {@link com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter}.  This
     * version allows for sending events do a specific Device (providing the address along side
     * the event name separated by a semi-colon).
     * <p>
     * This should generally only be used for {@link EventType#DEVICE_READ} events, but nothing
     * stops it from providing other types.
     *
     * @param event the {@link EventType} being sent to React Native JS
     * @param device the {@link NativeDevice} which caused/receiving the event
     * @param body the event content
     */
    synchronized private void sendEvent(EventType event, NativeDevice device, WritableMap body) {
        ReactContext context = getReactApplicationContext();

        if (context.hasActiveCatalystInstance()) {
            context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(String.format("%s@%s", event.name(), device.getAddress()), body);
        } else {
            Log.e(TAG, "There is currently no active Catalyst instance");
        }
    }

}
