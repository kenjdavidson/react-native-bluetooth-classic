
package kjd.reactnative.bluetooth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;

import kjd.reactnative.bluetooth.conn.ConnectionAcceptorFactory;
import kjd.reactnative.bluetooth.conn.ConnectionConnectorFactory;
import kjd.reactnative.bluetooth.conn.DelimitedStringDeviceConnectionImpl;
import kjd.reactnative.bluetooth.conn.DeviceConnectionFactory;
import kjd.reactnative.bluetooth.conn.RfcommAcceptorThreadImpl;
import kjd.reactnative.bluetooth.conn.RfcommConnectorThreadImpl;
import kjd.reactnative.bluetooth.conn.StandardOption;

/**
 *
 *
 * @author kenjdavidson
 *
 */
public class RNBluetoothClassicPackage implements ReactPackage {

    /**
     * Provides a default builder to allow for overriding.
     */
    public static final Builder DEFAULT_BUILDER
            = RNBluetoothClassicPackage.builder()
                .withConnectionFactory(StandardOption.CONNECTION_TYPE.defaultValue(), DelimitedStringDeviceConnectionImpl::new)
                .withConnectorFactory(StandardOption.CONNECTOR_TYPE.defaultValue(), RfcommConnectorThreadImpl::new)
                .withAcceptorFactory(StandardOption.ACCEPTOR_TYPE.defaultValue(), RfcommAcceptorThreadImpl::new);

    /**
     * {@link DeviceConnectionFactory} provide specific type of {@link kjd.reactnative.bluetooth.conn.DeviceConnection}
     * based on the type requested by the user.
     */
    private Map<String, DeviceConnectionFactory> mConnectionFactories;

    /**
     * {@link ConnectionAcceptorFactory} provide specific type of {@link kjd.reactnative.bluetooth.conn.ConnectionAcceptor}
     * based on the type requested by the user.
     */
    private Map<String, ConnectionAcceptorFactory> mAcceptorFactories;

    /**
     * {@link ConnectionConnectorFactory} provide specific type of {@link kjd.reactnative.bluetooth.conn.ConnectionConnector}
     * based on the type requested by the user.
     */
    private Map<String, ConnectionConnectorFactory> mConnectorFactories;

    /**
     * Creates a new package with the default {@link kjd.reactnative.bluetooth.conn.DeviceConnectionFactory}
     * for CLIENT and SERVER.  Sadly this needs to happen as I can't get the customized auto linking
     * working with a static variable definition.   I'll keep testing it out and if I can get it working
     * the constructors will be made private.
     */
    public RNBluetoothClassicPackage() {
        this.mConnectionFactories = Collections.singletonMap(
                StandardOption.CONNECTION_TYPE.defaultValue(),
                DelimitedStringDeviceConnectionImpl::new);
        this.mAcceptorFactories = Collections.singletonMap(
                StandardOption.ACCEPTOR_TYPE.defaultValue(),
                RfcommAcceptorThreadImpl::new);
        this.mConnectorFactories = Collections.singletonMap(
                StandardOption.CONNECTOR_TYPE.defaultValue(),
                RfcommConnectorThreadImpl::new);
    }

    /**
     * Provides the builder with a constructor.  This is the preferred method, but apprently the
     * auto linking doesn't work exactly as defined.
     * 
     * @param builder {@link Builder} used to create
     */
    private RNBluetoothClassicPackage(Builder builder) {
        this.mConnectionFactories = builder.mConnectionFactories;
        this.mAcceptorFactories = builder.mAcceptorFactories;
        this.mConnectorFactories = builder.mConnectorFactories;
    }

    /**
     * Provides the {@link RNBluetoothClassicModule} to the {@link com.facebook.react.ReactApplication}.
     *
     * @param reactContext the {@link ReactApplicationContext} provided by the React Native
     *                     application
     * @return array of modules provided by this package
     */
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        RNBluetoothClassicModule module = new RNBluetoothClassicModule(reactContext,
                mAcceptorFactories, mConnectorFactories, mConnectionFactories);
        return Arrays.<NativeModule>asList(module);
    }

    /**
     * @deprecated in version 0.47
     */
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    /**
     * There are currently no {@link ViewManager}(s) provided by this module.
     *
     * @param reactContext provided by the React Native application
     * @return empty list of {@link ViewManager}(s)
     */
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    /**
     * Provides a {@link Builder}.
     *
     * @return a {@link RNBluetoothClassicPackage.Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds {@link RNBluetoothClassicPackage}.  At this point there are only a
     * limited number of {@link DeviceConnectionFactory} options, but we need to leave
     * the door open to more.
     *
     * @author kendavidson
     */
    public static class Builder {
        private Map<String, DeviceConnectionFactory> mConnectionFactories;
        private Map<String, ConnectionAcceptorFactory> mAcceptorFactories;
        private Map<String, ConnectionConnectorFactory> mConnectorFactories;

        private Builder() {
            this.mConnectionFactories = new HashMap<>();
            this.mAcceptorFactories = new HashMap<>();
            this.mConnectorFactories = new HashMap<>();
        }

        public RNBluetoothClassicPackage build() {
            return new RNBluetoothClassicPackage(this);
        }

        public Builder withConnectionFactory(String type, DeviceConnectionFactory factory) {
            mConnectionFactories.put(type, factory);
            return this;
        }

        public Builder withAcceptorFactory(String type, ConnectionAcceptorFactory factory) {
            mAcceptorFactories.put(type, factory);
            return this;
        }

        public Builder withConnectorFactory(String type, ConnectionConnectorFactory factory) {
            mConnectorFactories.put(type, factory);
            return this;
        }
    }
}