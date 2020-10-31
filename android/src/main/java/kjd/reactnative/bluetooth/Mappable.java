package kjd.reactnative.bluetooth;

import com.facebook.react.bridge.WritableMap;

/**
 * Communication with React Native requires Objects to be converted to a
 * {@link com.facebook.react.bridge.WritableMap}.
 *
 * @author kendavidson
 */
public interface Mappable {

    /**
     * Implement to provide the {@link WritableMap} representation of this object.
     *
     * @return
     */
    WritableMap map();

}
