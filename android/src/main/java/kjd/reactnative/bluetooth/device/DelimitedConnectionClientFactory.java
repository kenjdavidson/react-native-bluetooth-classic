package kjd.reactnative.bluetooth.device;

public class DelimitedConnectionClientFactory implements DeviceConnectionFactory {
    @Override
    public DeviceConnection create() {
        return new DelimitedConnectionClientImpl();
    }
}
