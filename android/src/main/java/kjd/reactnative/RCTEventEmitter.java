package kjd.reactnative;

import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

/**
 * Needed to simulate the RCTEventEmitter from IOS in order to better manage the events so that
 * data is not lost due to the READ event, even if there are no READ listeners.
 * <p>
 * https://github.com/facebook/react-native/blob/master/React/Modules/RCTEventEmitter.h
 *
 * @deprecated with a commit to the Android {@link com.facebook.react.bridge.JavaModuleWrapper} the
 *          ability to add {@code @ReactMethod} to extended classes or implemented interfaces was
 *          removed (due to the method in which reflected methods are retrieved).  For that reason
 *          we've had to move this logic into the actual module.
 */
@Deprecated
public interface RCTEventEmitter {

    /**
     * Return the list of supported events, this determines whether the requested event can be
     * sent through to the NativeEventEmitter.js
     *
     * @return
     */
    default List<String> supportedEvents() {
        return Collections.emptyList();
    }

    /**
     * Get the {@link ReactContext} from the implementing
     * {@link com.facebook.react.module.annotations.ReactModule}.
     *
     * @return
     */
    ReactContext getReactContext();

    /**
     * Sending an event, this simulates the same send event functionality as on IOS, using the
     * Android specifics.  Events are only sent when the context is available and there are
     * currently listeners accepting (this is not done directly in IOS, but was added here).
     *
     * @param eventName name of the event to be sent
     * @param body content of the event to be sent
     */
    default void sendEvent(String eventName, @Nullable WritableMap body) {
        ReactContext context = getReactContext();

        if (context.hasActiveCatalystInstance()) {
            context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, body);
        } else {
            Log.e(RCTEventEmitter.class.getSimpleName(),
                    String.format("%s has no active Catalyst instance", RCTEventEmitter.class.getSimpleName()));
        }
    }

    /**
     * startObserving from IOS version.  Default implementation does nothing.
     */
    default void startObserving() {}

    /**
     * stopObserving from IOS version.  Default implementation does nothing.
     */
    default void stopObserving() {}

    /**
     * addListener from IOS version.
     *
     * @param eventName
     */
    void addListener(String eventName);

    /**
     * removeListeners from IOS version.
     *
     * @param count
     */
    void removeListeners(int count);

    /**
     * Sadly the IOS implementation doesn't provide a removeListeners which contains
     * an {@code eventName}.  So in both IOS and Android we need to provide such method
     * and call it only on reads.
     * <p>
     * There are better ways to do this, but to keep IOS and Android in line, was the
     * fastest at the time.
     *
     * @param address
     */
    void removeListener(String address);

}
