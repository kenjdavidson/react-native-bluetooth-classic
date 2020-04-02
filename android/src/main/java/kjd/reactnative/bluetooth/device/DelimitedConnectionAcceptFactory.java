package kjd.reactnative.bluetooth.device;

public class DelimitedConnectionAcceptFactory implements DeviceConnectionFactory {
    @Override
    public DeviceConnection create() {
        return new DelimitedConnectionAcceptImpl();
    }
}
