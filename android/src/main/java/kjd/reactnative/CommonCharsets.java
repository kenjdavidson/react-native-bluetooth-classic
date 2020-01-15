package kjd.reactnative;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.nio.charset.Charset;

/**
 * Provides extended common charsets across Android and IOS.
 */
public enum CommonCharsets {
    LATIN("ISO_8859_1"),
    ASCII("US_ASCII"),
    UTF8("UTF_8"),
    UTF16("UTF_16");

    private final String _code;
    private final Charset _charset;

    private CommonCharsets(String code) {
        this._code = code;
        this._charset = Charset.forName(code);
    }

    public static WritableMap asMap() {
        WritableMap map = Arguments.createMap();
        for (CommonCharsets charset : CommonCharsets.values()) {
            map.putString(charset.name(), charset.name());
        }
        return map;
    }

    public String code() {
        return _code;
    }

    public Charset charset() {
        return _charset;
    }
}
