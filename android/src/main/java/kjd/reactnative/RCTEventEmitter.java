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
 *
 * https://github.com/facebook/react-native/blob/master/React/Modules/RCTEventEmitter.h
 */
public interface RCTEventEmitter {

    /**
     * Current number of listeners.  An AtomicInteger might be overkill, but since the docs say
     * we can't make guarantees about which thread being called, so play it safe.  If this causes
     * issues, we can use a counter type.
     */
    AtomicInteger listenerCount = new AtomicInteger(0);

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
     * Return the current ReactContext.
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
        if (listenerCount.get() > 0 && context.hasActiveCatalystInstance()) {
            Log.d(RCTEventEmitter.class.getSimpleName(),
                    String.format("Sending event [%s] with data [%s]", eventName, body));

            context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, body);
        }
    }

    /**
     * Set any required settings for starting to observe.
     */
    default void startObserving() {}

    /**
     * Handle any requirements for when observing is stopped.
     */
    default void stopObserving() {}

    /**
     * Called by the NativeEventEmitter.js when a Subscription/Listener is added.
     *
     * @param eventName event name for which listener was added
     */
    @ReactMethod
    default void addListener(String eventName) {
        int currentCount = listenerCount.incrementAndGet();

        Log.d(RCTEventEmitter.class.getSimpleName(),
                String.format("Adding listener to %s, currently have %d listeners",
                        eventName, currentCount));
        if (1 == currentCount) {
            startObserving();
        }
    }

    /**
     * Called by the NativeEventEmitter (React Native) when a subscription/listener is removed.
     *
     * @param count number of listeners removed
     */
    @ReactMethod
    default void removeListeners(int count) {
        int currentCount = listenerCount.get();
        if (count > currentCount) {
            Log.d(RCTEventEmitter.class.getSimpleName(),
                    String.format("Attempted to remove more listeners than added"));
        }

        currentCount = Math.max(currentCount - count, 0);
        listenerCount.set(currentCount);

        if (0 == currentCount) {
            stopObserving();
        }
    }
}
