
import React, {Component} from 'react';
import {Platform, StyleSheet, Text, TextInput, View, ScrollView, TouchableOpacity} from 'react-native';
import RNBluetoothClassic, {BTEvents} from '@kenjdavidson/react-native-bluetooth-classic';
import Toast, {DURATION} from 'react-native-easy-toast'
import Buffer from 'buffer';

const formatDate = (date) => {
  return date.getUTCFullYear() + "/" +
    ("0" + (date.getUTCMonth()+1)).slice(-2) + "/" +
    ("0" + date.getUTCDate()).slice(-2) + " " +
    ("0" + date.getUTCHours()).slice(-2) + ":" +
    ("0" + date.getUTCMinutes()).slice(-2) + ":" +
    ("0" + date.getUTCSeconds()).slice(-2);
}

const DeviceList = ({devices, onPress, style}) => 
  <ScrollView style={styles.listContainer} contentContainerStyle={styles.container}>
    {devices.map((device,i) => {
      return (
        <TouchableOpacity key={device.id} style={[ styles.button, style ]} onPress={() => onPress(device)}>
          <View style={styles.connectionStatus}></View>
          <View style={{flex: 1}}>
            <Text style={styles.deviceName}>{device.name}</Text>
            <Text>{device.address}</Text>
          </View>
        </TouchableOpacity>
      )
    })}
  </ScrollView>

const DeviceConnection = ({device, scannedData}) => 
  <View style={styles.container}>
    <ScrollView style={{flex: 1}} contentContainerStyle={{justifyContent:'flex-end'}}>
      {scannedData.map((data) => {
        return (
          <View key={data.timestamp.toISOString()} style={{flexDirection:'row', justifyContent:'flex-start'}}>
            <Text>{formatDate(data.timestamp)}</Text>
            <Text>{' > '}</Text>
            <Text>{data.data}</Text>
          </View>          
        );
      })}
    </ScrollView>
    <View style={[styles.horizontalContainer, {backgroundColor: '#ccc'}]}>
      <TextInput style={{flex:1}} placeholder={'Send Data'}></TextInput>
      <TouchableOpacity style={{justifyContent: 'center'}}>
        <Text>Send</Text>
      </TouchableOpacity>
    </View>    
  </View>

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
    this.initializeDevices();

    RNBluetoothClassic.addListener(BTEvents.READ, this.handleRead);
  }

  componentWillUnmount() {
    RNBluetoothClassic.disconnect();
    RNBluetoothClassic.removeAllListeners();
  }

  handleRead = (data) => {
    data.timestamp = new Date();   
    let scannedData = this.state.scannedData;
    scannedData.push(data); 

    console.log(scannedData);
    this.setState({scannedData});
  }

  async initializeDevices() {
    try {
      let connectedDevice = await RNBluetoothClassic.getConnectedDevice();
      this.setState({connectedDevice});
    } catch(error) {
      let devices = await RNBluetoothClassic.list();

      console.log(devices);
      this.setState({deviceList: devices}); 
    }   
  }

  async connectToDevice(device) {
    console.log(`Attempting connection to device ${device.id}`);
    try {
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
    let title = !this.state.connectedDevice ? 'Connected Devices' : this.state.connectedDevice.name;    
    return (
      <View style={styles.container}>
        <View style={styles.toolbar}>
          <Text style={styles.toolbarText}>{title}</Text>
          {this.state.connectedDevice != undefined
            ? (<Text style={styles.toolbarButton} onPress={this.unselectDevice}>X</Text>) 
            : null
          }
        </View>
        {!this.state.connectedDevice
          ? <DeviceList devices={this.state.deviceList}
              onPress={this.selectDevice}></DeviceList> 
          : <DeviceConnection device={this.state.device} scannedData={this.state.scannedData}></DeviceConnection>
        }    
        <Toast ref="toast"></Toast>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  toolbar: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    backgroundColor: '#222',
    paddingTop: 8,
    paddingBottom: 8,
    paddingLeft: 16,
    paddingRight: 16,
    height: 52
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
  }
});
