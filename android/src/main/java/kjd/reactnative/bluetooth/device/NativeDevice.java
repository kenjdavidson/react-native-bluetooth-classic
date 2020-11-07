package kjd.reactnative.bluetooth.device;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

import kjd.reactnative.bluetooth.Mappable;

/**
 * Provides wrapping of {@link android.bluetooth.BluetoothDevice} details and communication.
 * Primarily used for providing the {@link Mappable#map()} method.
 *
 * @author kendavidson
 */
public class NativeDevice implements Mappable {

    private BluetoothDevice mDevice;
    private Map<String,Object> mExtra;

    public NativeDevice(BluetoothDevice device) {
        this.mDevice = device;
        this.mExtra = new HashMap<>();
    }

    public BluetoothDevice getDevice() { return mDevice; }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public String getName() {
        return mDevice.getName();
    }

    public int getBondState() {
        return mDevice.getBondState();
    }

    public BluetoothClass getBluetoothClass() {
        return mDevice.getBluetoothClass();
    }

    public ParcelUuid[] getUuids() {
        return mDevice.getUuids();
    }

    public <T> T getExtra(String key) {
        return (T) mExtra.get(key);
    }

    public <T> T putExtra(String key, T value) {
        return (T) mExtra.put(key, value);
    }

    @Override
    public WritableMap map() {
        WritableMap mapped = Arguments.createMap();

        mapped.putString("name", mDevice.getName() != null ? mDevice.getName() : mDevice.getAddress());
        mapped.putString("address", mDevice.getAddress());
        mapped.putString("id", mDevice.getAddress());
        mapped.putBoolean("bonded", mDevice.getBondState() == BluetoothDevice.BOND_BONDED);

        if (mDevice.getBluetoothClass() != null) {
            WritableMap deviceClass = Arguments.createMap();
            deviceClass.putInt("deviceClass", mDevice.getBluetoothClass().getDeviceClass());
            deviceClass.putInt("majorClass", mDevice.getBluetoothClass().getMajorDeviceClass());
        }

        mapped.putMap("extra", Arguments.makeNativeMap(mExtra));

        return mapped;
    }
}
