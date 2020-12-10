package kjd.reactnative.bluetooth.conn;

import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Provides some standardized options and method for parsing their values (and default values).
 * They're al lumped here, as opposed to being split into their specific packages/classes,
 * just because.
 * <p>
 * Feel free to re-use these options in your custom acceptors, connectors and connections; or
 * provide your own; or just use straight strings to manage them.
 *
 * @author kendavidson
 *
 */
public enum StandardOption {
    /**
     * Allows React Native app to specify the {@link ConnectionConnector} to use when performing
     * a {@code connectToDevice} request.  The default is {@code rfcomm} which is provided by
     * the {@link RfcommConnectorThreadImpl}.
     */
    CONNECTOR_TYPE("connectorType", String.class, "rfcomm"),

    /**
     * Allows React Native app to specify the {@link ConnectionAcceptor} to use when performing
     * a {@code accept} request.  The default is {@code rfcomm} which is provided by
     * the {@link RfcommAcceptorThreadImpl}.
     */
    ACCEPTOR_TYPE("acceptorType", String.class, "rfcomm"),

    /**
     * Allows React Native app to specify the {@link DeviceConnection} to be used after a
     * successful {@code accept} or {@code connectToDevice} request.  The default is {@code delimtied}
     * which supplies the {@link DelimitedStringDeviceConnectionImpl}.
     */
    CONNECTION_TYPE("connectionType", String.class, "delimited"),

    /**
     * Provide a {@code delimiter} String to your {@link DeviceConnection}.  This will really only
     * be used with any type of delimited or String based connection.
     */
    DELIMITER("delimiter", String.class, "\n"),

    /**
     * The {@link Charset} to be used during the encoding/decoding of messages from the device.
     * Charset provides it's own conversion as it may need to perform a string conversion
     * during processing.
     */
    DEVICE_CHARSET("charset", Charset.class, Charset.forName("ascii")) {
        @Override
        public <T> T get(Properties properties) {
            Object value = properties.containsKey(this.name())
                    ? properties.get(this.name()) : properties.containsKey(this.name().toLowerCase())
                        ? properties.get(this.name().toLowerCase()) : properties.get(this.code());
            if (value == null) {
                value = Charset.forName("ascii");
            } else if (value instanceof String) {
                value = Charset.forName((String) value);
            }

            return (T) value;
        }
    },

    /**
     * Provide a timeout between individual reads of the device.  This is a hold over from the
     * original project and in most forks has been removed.   The original library had 500
     * as the default timeout, but this was changed to 0 as it was causing a lot of people
     * slowdowns.
     *
     */
    READ_TIMEOUT("readTimeout", Integer.class, 0   ),

    /**
     * Another hold over from the original was the max size of the buffer.  This defaults to {@code 1024}
     * but in some forks has been increased to allow for more data in a single read.
     */
    READ_SIZE("readSize", Integer.class, 1024),

    /**
     * Used by {@link ConnectionAcceptor} and {@link ConnectionConnector} to determine whether to
     * use secure or insecure sockets.
     */
    SECURE_SOCKET("secure", Boolean.class, true),

    /**
     * Used by {@link ConnectionAcceptor} to set a name when other devices find this.
     */
    SERVICE_NAME("serviceName", String.class, "RNBluetoothClassic"),

    /**
     * Currently the default {@link ConnectionAcceptor} allows a single connection then returns
     * the device.  This was put in to eventually allow multiple connections to be found.  Although
     * this requires a bit of addition to make it worth while, like notifying of a connected device
     * upon each accept.   It needs more testing and work so it's just here for reference.
     */
    ACCEPT_CONNECTION_NUM("acceptConnectionNumber", Integer.class, 1);

    private String code;
    private Class clazz;
    private Object defaultValue;

    StandardOption(String code, Class clazz, Object defaultValue) {
        this.code = code;
        this.clazz = clazz;
        this.defaultValue = defaultValue;
    }

    public Class type() {
        return clazz;
    }

    public String code() {
        return code;
    }

    /**
     * Get the current or default value of the {@link StandardOption}.  The property key is checked
     * in order:
     * <ul>
     *     <li>StandardOption name</li>
     *     <li>StandardOption name as lowercase</li>
     *     <li>StandardOption code</li>
     * </ul>
     * which I'm not a huge fan of, but this will allow for a number of different methodologies
     * with regards to properties and naming conventions.  In the end hopefully we can
     * stick with one.
     *
     * @param properties the {@link Properties} provided by the application
     *
     * @param <T> the Type of data this option represents.  Be careful here as this does no
     *           type checking.
     * @return the provided or default value
     */
    public <T> T get(Properties properties) {
        Object value = properties.containsKey(this.name())
            ? properties.get(this.name()) : properties.containsKey(this.name().toLowerCase())
                ? properties.get(this.name().toLowerCase()) : properties.get(this.code());

        if (value == null || !(this.type().isAssignableFrom(value.getClass()))) {
            return this.defaultValue();
        }

        return (T) value;
    }

    /**
     * The default value.
     *
     * @param <T> the Type of data this option represents.  Be careful here as this does no
     *          type checking.
     * @return
     */
    public <T> T defaultValue() {
        return (T) defaultValue;
    }
}
