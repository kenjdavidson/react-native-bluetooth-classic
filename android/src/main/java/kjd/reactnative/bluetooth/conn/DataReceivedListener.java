package kjd.reactnative.bluetooth.conn;

import kjd.reactnative.bluetooth.device.NativeDevice;

public interface DataReceivedListener {

    void onDataReceived(NativeDevice device, String data);

}
