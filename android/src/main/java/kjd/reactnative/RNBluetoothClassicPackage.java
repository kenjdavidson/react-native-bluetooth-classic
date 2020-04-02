
package kjd.reactnative;

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
import kjd.reactnative.bluetooth.device.DelimitedConnectionAcceptFactory;
import kjd.reactnative.bluetooth.device.DelimitedConnectionClientFactory;
import kjd.reactnative.bluetooth.device.DeviceConnectionFactory;

/**
 * {@link ReactPackage} provides a method for applications to implement customized
 * {@link NativeModule}(s).  Prior to version {@code 0.60.0} a plain old
 * {@code new RNBluetoothClassicPackage()} was used, with auto-linking, it's still required to
 * manually add the {@link ReactPackage} (due to the private constructor).  It would be possible
 * to leave it public and do some other things, but this seems to be the safest bet.
 * <p>
 * The standard/default package has the following connections configured:
 * <ul>
 *     <li><strong>connect</strong> connection using {@link kjd.reactnative.bluetooth.device.DelimitedConnectionClientImpl}
 *     which connects as a client using the standard delimited messaging.</li>
 *     <li><strong>accept</strong> connection using {@link kjd.reactnative.bluetooth.device.DelimitedConnectionAcceptImpl}
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

    public static final RNBluetoothClassicPackage DEFAULT
            = RNBluetoothClassicPackage.builder()
                .withFactory(ConnectionType.CLIENT.name(), new DelimitedConnectionClientFactory())
                .withFactory(ConnectionType.SERVER.name(), new DelimitedConnectionAcceptFactory())
                .build();

    private Map<String, DeviceConnectionFactory> mFactories;

    /**
     * Creates a {@link RNBluetoothClassicPackage} - the {@link Builder} or standard packages
     * should be used.
     *
     * @param factories
     */
    private RNBluetoothClassicPackage(Map<String,DeviceConnectionFactory> factories) {
        this.mFactories = new HashMap<>(factories);
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