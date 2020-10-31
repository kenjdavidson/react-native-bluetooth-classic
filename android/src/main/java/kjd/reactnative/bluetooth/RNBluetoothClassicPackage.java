
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

import kjd.reactnative.bluetooth.conn.ConnectionType;
import kjd.reactnative.bluetooth.conn.DelimitedConnectionAcceptImpl;
import kjd.reactnative.bluetooth.conn.DelimitedConnectionClientImpl;
import kjd.reactnative.bluetooth.conn.DeviceConnectionFactory;

/**
 * {@link ReactPackage} provides a method for applications to implement customized
 * {@link NativeModule}(s).  Prior to version {@code 0.60.0} a plain old
 * {@code new RNBluetoothClassicPackage()} was used, with auto-linking, it's still required to
 * manually add the {@link ReactPackage} (due to the private constructor).  It would be possible
 * to leave it public and do some other things, but this seems to be the safest bet.
 * <p>
 * The standard/default package has the following connections configured:
 * <ul>
 *     <li><strong>connect</strong> connection using {@link DelimitedConnectionClientImpl}
 *     which connects as a client using the standard delimited messaging.</li>
 *     <li><strong>accept</strong> connection using {@link DelimitedConnectionAcceptImpl}
 *     which accepts a connection from a device using the standard delimited messaging.</li>
 * </ul>
 * The package should be added to your {@code MainApplication} using the following:
 * <pre><code>
 * List<ReactPackage> packages = new PackageList(this).getPackages();
 * // Packages that cannot be autolinked yet can be added manually here, for example:
 * // packages.add(new MyReactNativePackage());
 * packages.add(RNBluetoothClassicPackage.DEFAULT);
 * </code></pre>
 * if more customized connections are required, you should:
 * <ul>
 *     <li>Override or implement the required changes</li>
 *     <li>Apply the default/custom implementations into the Package factories</li>
 *     <li>Add the package in your {@code MainApplication}</li>
 * </ul>
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
                .withFactory(ConnectionType.CLIENT.name(), () -> new DelimitedConnectionClientImpl())
                .withFactory(ConnectionType.SERVER.name(), () -> new DelimitedConnectionAcceptImpl());

    /**
     * {@link DeviceConnectionFactory} map which will be passed into the module.  The factory map
     * is used during the connection process by passing the TYPE option into either
     * connect or accept.  The value of this type will attempt to grab the correct type from
     * the Factory assigned.
     */
    private Map<String, DeviceConnectionFactory> mFactories;

    /**
     * Creates a new package with the default {@link kjd.reactnative.bluetooth.conn.DeviceConnectionFactory}
     * for CLIENT and SERVER.
     */
    public RNBluetoothClassicPackage() {
        this.mFactories = new HashMap<>();
        this.mFactories .put(ConnectionType.CLIENT.name(), () -> new DelimitedConnectionClientImpl());
        this.mFactories .put(ConnectionType.SERVER.name(), () -> new DelimitedConnectionAcceptImpl());
    }

    /**
     * Provides the builder with a constructor.  This is the preferred method, but apprently the
     * autolinking doesn't work exactly as defined.
     * 
     * @param factories
     */
    private RNBluetoothClassicPackage(Map<String,DeviceConnectionFactory> factories) {
        this.mFactories = factories;
    }

    /**
     * Provides the {@link RNBluetoothClassicModule} to the {@link com.facebook.react.ReactApplication}.
     *
     * @param reactContext
     * @return
     */
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new RNBluetoothClassicModule(reactContext, mFactories));
    }

    /**
     * Previously required by {@link ReactPackage}.
     *
     * @return
     *
     * @deprecated in version 0.47
     */
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    /**
     * There are currently no {@link ViewManager}(s) provided by this module.
     *
     * @param reactContext {@link ReactApplicationContext}
     * @return
     */
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    /**
     * Provides a {@link Builder}.
     *
     * @return
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
        private Map<String, DeviceConnectionFactory> mFactories;

        private Builder() {
            this.mFactories = new HashMap<>();
        }

        public RNBluetoothClassicPackage build() {
            return new RNBluetoothClassicPackage(mFactories);
        }

        public Builder withFactory(String type, DeviceConnectionFactory factory) {
            mFactories.put(type, factory);
            return this;
        }
    }
}