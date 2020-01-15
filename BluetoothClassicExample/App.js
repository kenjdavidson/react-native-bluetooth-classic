
import React from 'react';
import { Platform, 
  StyleSheet, 
  Text, 
  TextInput, 
  View, 
  ScrollView,
  FlatList, 
  TouchableOpacity, 
  StatusBar,
  KeyboardAvoidingView
} from 'react-native';
import RNBluetoothClassic, { BTEvents, BTCharsets } from '@kenjdavidson/react-native-bluetooth-classic';
import Toast, { DURATION } from 'react-native-easy-toast'
import KeyboardSpacer from 'react-native-keyboard-spacer';

const formatDate = (date) => {
  return date.getUTCFullYear() + "/" +
    ("0" + (date.getUTCMonth()+1)).slice(-2) + "/" +
    ("0" + date.getUTCDate()).slice(-2) + " " +
    ("0" + date.getUTCHours()).slice(-2) + ":" +
    ("0" + date.getUTCMinutes()).slice(-2) + ":" +
    ("0" + date.getUTCSeconds()).slice(-2);
}

const AppStatusBar = ({backgroundColor, ...props}) => (
  <View style={[styles.statusbar, {backgroundColor}]}>
    <StatusBar translucent backgroundColor={backgroundColor} {...props} />
  </View> 
) 

const DeviceList = ({devices, onPress, style}) => {
  return (
    <ScrollView style={styles.listContainer} contentContainerStyle={styles.container}>
      {devices.map((device,i) => {
        let bgColor = device.connected ? '#0f0' : styles.connectionStatus.backgroundColor
        return (
          <TouchableOpacity key={device.id} style={[ styles.button, style ]} onPress={() => onPress(device)}>
            <View style={[styles.connectionStatus, {backgroundColor: bgColor}]}></View>
            <View style={{flex: 1}}>
              <Text style={styles.deviceName}>{device.name}</Text>
              <Text>{device.address}</Text>
            </View>
          </TouchableOpacity>
        )
      })}
    </ScrollView>
  )
}

class DeviceConnection extends React.Component {
  constructor(props) {
    super(props);
    this.state = { 
      text: undefined,
      scannedData: []
    };
  }

  componentWillMount() {     
    this.onRead = RNBluetoothClassic.addListener(BTEvents.READ, this.handleRead, this);
    //this.poll = setInterval(() => this.pollForData(), 3000);
  }

  componentWillUnmount() {
    this.onRead.remove();
    //clearInterval(this.poll);

    RNBluetoothClassic.disconnect();    
  }

  pollForData = async () => {
    var available = 0;

    do {
      console.log('Checking for available data');
      available = await RNBluetoothClassic.available();
      console.log(`There are ${available} bytes of data available`);

      if (available > 0) {
        console.log(`Attempting to read the next message from the device`);
        const data = await RNBluetoothClassic.readFromDevice();

        console.log(data);
        this.handleRead({data});
      }
    } while (available > 0);
  }

  handleRead = (data) => {
    data.timestamp = new Date();   
    let scannedData = this.state.scannedData;
    scannedData.unshift(data); 
    this.setState({scannedData});
  }

  sendData = async () => {
    let message = this.state.text + '\r';   // For commands
    await RNBluetoothClassic.write(message);
  
    let scannedData = this.state.scannedData;
    scannedData.push({
      timestamp: new Date(),
      data: this.state.text,
      type: 'sent'
    });
    this.setState({text: "", scannedData});    
  }

  render() {
    const keyboardVerticalOffset = Platform.OS === 'ios' ? 40 : 0
    return (
      <View style={styles.container}>
        <View style={styles.toolbar}>
          <Text style={styles.toolbarText}>{this.props.device.name}</Text>
          <TouchableOpacity  onPress={this.props.disconnect}>
            <Text style={[styles.toolbarButton, {color:'#F00'}]}>X</Text>       
          </TouchableOpacity>
        </View>    
        <View style={{flex: 1}}>
          <FlatList style={{flex: 1}} contentContainerStyle={{justifyContent:'flex-end'}}
            inverted
            ref='scannedDataList'
            data={this.state.scannedData}
            keyExtractor={(item,index) => item.timestamp.toISOString()}
            renderItem={({item}) => (
              <View id={item.timestamp.toISOString()} style={{flexDirection:'row', justifyContent:'flex-start'}}>
                <Text>{formatDate(item.timestamp)}</Text>
                <Text>{item.type === 'sent' ? ' < ' : ' > '}</Text>
                <Text style={{ flexShrink: 1 }}>{item.data.trim()}</Text>
              </View>)}>
          </FlatList>
          <View style={[styles.horizontalContainer, {backgroundColor: '#ccc'}]}>
            <TextInput style={styles.textInput}             
              placeholder={'Send Data'}
              value={this.state.text}
              onChangeText={(text) => this.setState({text})}
              autoCapitalize='none'
              autoCorrect={false}
              onSubmitEditing={() => this.sendData()}
              returnKeyType='send'></TextInput>
            <TouchableOpacity style={{justifyContent: 'center'}} 
              onPress={() => this.sendData()}>
              <Text>Send</Text>
            </TouchableOpacity>
          </View>            
        </View>
        { Platform.OS == 'ios' ? <KeyboardSpacer/> : null}        
      </View>
    )
  }
}

export default class App extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      deviceList: [],
      connectedDevice: undefined,
      scannedData: []
    }
  }

  componentWillMount() {     
    this.initialize();

    this.subs = [];

    // Re-initialize whenever a Bluetooth event occurs
    this.subs.push(RNBluetoothClassic.addListener(BTEvents.BLUETOOTH_CONNECTED, 
        (device) => this.onConnected(device), this));
    this.subs.push(RNBluetoothClassic.addListener(BTEvents.BLUETOOTH_DISCONNECTED, 
        (device) => this.onDisconnected(device), this));
    this.subs.push(RNBluetoothClassic.addListener(BTEvents.CONNECTION_LOST,
        (error) => this.onConnectionLost(error), this));
    this.subs.push(RNBluetoothClassic.addListener(BTEvents.ERROR,
        (error) => this.onError(error), this))
  }

  componentWillUnmount() {
    this.subs.forEach( sub => sub.remove() );
  }

  onConnected(device) {
    this.refs.toast.show(`Connected to ${device.name}`, DURATION.LENGTH_SHORT);
    this.initialize();
  }

  onDisconnected(device) {
    this.refs.toast.show(`Connection to ${device.name} was disconnected`, DURATION.LENGTH_SHORT);
    this.initialize();
  }

  onConnectionLost(error) {
    this.refs.toast.show(`Connection to ${error.device.name} was lost`, DURATION.LENGTH_SHORT);
    this.initialize();
  }

  onError(error) {
    this.refs.toast.show(`${error.message}`, DURATION.LENGTH_SHORT);
  }

  async initialize() {
    let enabled = await RNBluetoothClassic.isEnabled();
    let newState = { 
      bluetoothEnabled: enabled, 
      devices: [], 
      connectedDevice: undefined
    };

    if (enabled) {
      try {
        let connectedDevice = await RNBluetoothClassic.getConnectedDevice();

        console.log('initializeDevices::connectedDevice')
        console.log(connectedDevice)
        newState.connectedDevice = connectedDevice;
      } catch(error) {
        try {
          let devices = await RNBluetoothClassic.list();
  
          console.log('initializeDevices::list')
          console.log(devices);
          newState.deviceList = devices;
        } catch(error) {
          console.error(error.message);
        }     
      }
    }   

    this.setState(newState);
  }

  async connectToDevice(device) {
    console.log(`Attempting connection to device ${device.id}`);
    try {
      await RNBluetoothClassic.setEncoding(BTCharsets.ASCII);
      let connectedDevice = await RNBluetoothClassic.connect(device.id);
      this.setState({connectedDevice});
      this.refs.toast.close();
    } catch (error) {
      console.log(error.message);
      this.refs.toast.show(`Connection to ${device.name} unsuccessful`, DURATION.LENGTH_SHORT);
    }    
  }

  async disconnectFromDevice() {
    await RNBluetoothClassic.disconnect();
    this.setState({connectedDevice: undefined})
  }

  selectDevice = (device) => this.connectToDevice(device);
  unselectDevice = () => this.disconnectFromDevice();

  render() {
    let connectedColor = !this.state.bluetoothEnabled ? styles.toolbarButton.color : 'green';
    return (
      <View style={styles.container}>
        <AppStatusBar backgroundColor={styles.statusbar.backgroundColor} barStyle="light-content"></AppStatusBar>
        <View style={styles.toolbar}>
          <Text style={styles.toolbarText}>Bluetooth Devices</Text>
          <Text style={[styles.toolbarButton, {color: connectedColor}]}>O</Text>
        </View>
        {!this.state.connectedDevice
          ? <DeviceList 
              devices={this.state.deviceList}
              onPress={this.selectDevice}></DeviceList> 
          : <DeviceConnection 
              device={this.state.connectedDevice} 
              scannedData={this.state.scannedData}
              disconnect={this.unselectDevice}
              onSend={this.onSend}></DeviceConnection>
        }    
        <Toast ref="toast"></Toast>
      </View>
    );
  }
}

/**
 * The statusbar height goes wonky on Huawei with a notch - not sure if its the notch or the
 * Huawei but the fact that the notch is different than the status bar makes the statusbar 
 * go below the notch (even when the notch is on).
 */
const STATUSBAR_HEIGHT = Platform.OS === 'ios' ? 20 : StatusBar.currentHeight;
const APPBAR_HEIGHT = Platform.OS === 'ios' ? 44 : 56;

const styles = StyleSheet.create({
  statusbar: {
    height: STATUSBAR_HEIGHT,
    backgroundColor: '#222'
  },
  toolbar: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    backgroundColor: '#222',
    paddingTop: 8,
    paddingBottom: 8,
    paddingLeft: 16,
    paddingRight: 16,
    height: APPBAR_HEIGHT
  },
  toolbarText: {
    flex: 1,
    fontSize: 20,
    color: '#fff' 
  },
  toolbarButton: {
    fontSize: 20,
    color: '#fff'
  },
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'stretch',
    backgroundColor: '#fff',
  },
  listContainer: {
    flex: 1
  },
  button: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'stretch',
    paddingTop: 8,
    paddingBottom: 8,
    paddingLeft: 16,
    paddingRight: 16
  },  
  deviceName: {
    fontSize: 16,    
  },
  connectionStatus: {
    width: 8,
    backgroundColor: '#ccc',
    marginRight: 16,
    marginTop: 8,
    marginBottom: 8
  },
  horizontalContainer: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'stretch',
    paddingLeft: 16,
    paddingRight: 16,
    paddingTop: 8,
    paddingBottom: 8
  },
  textInput: {
    flex: 1,
    height: 40
  }
});
