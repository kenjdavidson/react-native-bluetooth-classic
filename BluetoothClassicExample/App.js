import React from 'react';
import {
  Root,
  StyleProvider,
} from 'native-base';
import RNBluetoothClassic, {
  BluetoothEventType,
  BluetoothErrors
} from 'react-native-bluetooth-classic';
import getTheme from './native-base-theme/components';
import platform from './native-base-theme/variables/platform';
import ConnectionScreen from './src/connection/ConnectionScreen';
import DeviceListScreen from './src/device-list/DeviceListScreen';

export default class App extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      device: undefined,
      bluetoothEnabled: true
    };
  }

  /**
   * Sets the current device to the application state.  This is super basic 
   * and should be updated to allow for things like:
   * - multiple devices
   * - more advanced state management (redux)
   * - etc
   * 
   * @param device the BluetoothDevice selected or connected
   */
  selectDevice = (device) => {
    console.log(`App::selectDevice`)
    console.log(device);

    this.setState({ device });
  }

  /**
   * On mount:
   * - setup the connect and disconnect listeners
   * - determine if bluetooth is enabled (may be redundant with listener)
   */
  async componentDidMount() {
    console.log(`App::componentDidMount adding listeners`)
    this.enabledSubscription = RNBluetoothClassic
      .onBluetoothEnabled((event) => this.onStateChanged(event));
    this.disabledSubscription = RNBluetoothClassic
      .onBluetoothDisabled((event) => this.onStateChanged(event));

    try {
      console.log(`App::componentDidMount checking bluetooth status`);
      let enabled = await RNBluetoothClassic.isBluetoothEnabled();

      console.log(`App::componentDidMount status => ${enabled}`);
      this.setState({ bluetoothEnabled: enabled });
    } catch (error) {
      console.log(`App::componentDidMount error`, error);
      this.setState({ bluetoothEnabled: false});
    }
  }

  /**
   * Clear subscriptions
   */
  componentWillUnmount() {
    console.log(`App:componentWillUnmount removing subscriptions`)
    this.enabledSubscription.remove()
    this.disabledSubscription.remove();
  }

  /**
   * Handle state change events.
   * 
   * @param stateChangedEvent event sent from Native side during state change
   */
  onStateChanged(stateChangedEvent) {
    console.log(`App::onStateChanged`)
    console.log(stateChangedEvent);
    
    this.setState({
      bluetoothEnabled: stateChangedEvent.enabled,
      device: stateChangedEvent.enabled ? this.state.device : undefined
    });
  }

  render() {
    return (
      <StyleProvider style={getTheme(platform)}>
        <Root>
          {!this.state.device ? (
            <DeviceListScreen
              bluetoothEnabled={this.state.bluetoothEnabled}
              selectDevice={this.selectDevice} />
          ) : (
            <ConnectionScreen 
              device={this.state.device}
              onBack={() => this.setState({device: undefined})}/>
          )}
        </Root>
      </StyleProvider>
    );
  }
}
