package kjd.reactnative.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.facebook.react.bridge.WritableMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import kjd.reactnative.bluetooth.device.NativeDevice;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class BluetoothExceptionTest extends TestCase {

    @Test
    public void test_bluetoothException_string() {
        String message = "message";
        BluetoothException be = new BluetoothException(message);
        WritableMap map = be.map();

        assertEquals(message, map.getString(BluetoothException.MESSAGE));

        assertFalse(map.hasKey(BluetoothException.DEVICE));
        assertEquals("test_message", map.getString(BluetoothException.MESSAGE));
        assertFalse(map.hasKey(BluetoothException.ERROR));
    }

    @Test
    public void test_bluetoothException_string_cause() {
        String message = "message";
        Exception exception = new Exception("exception");
        BluetoothException be = new BluetoothException(message, exception);
        WritableMap map = be.map();

        assertFalse(map.hasKey(BluetoothException.DEVICE));
        assertEquals(message, map.getString(BluetoothException.MESSAGE));
        assertEquals(exception.getLocalizedMessage(), map.getString(BluetoothException.ERROR));
    }

    @Test
    public void test_bluetoothException_device_string_cause() {
        fail("Not yet implemented");
    }
}