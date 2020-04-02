import React, {Component} from 'react';
import RNBluetoothClassic, { BluetoothEvents } from 'react-native-bluetooth-classic';
import { Container, Text, Header, Left, Button, Icon, Body, Title, Subtitle } from 'native-base';
import { FlatList, View, StyleSheet, TextInput, TouchableOpacity, BackHandler } from 'react-native';
import Logger from '../common/logger';

/**
 * Manages a selected device connection.  The selected Device should 
 * be provided as {@code props.device}, the device will be connected 
 * to and processed as such.
 * 
 * @author kendavidson
 */
export default class ConnectionScreen extends React.Component {
  constructor(props) {
    super(props);    

    this.state = {
      text: undefined,
      data: [{
        data: `Attempting connection to ${props.device.address}`,
        timestamp: new Date(),
        type: 'error'
      }],
      polling: false,
      connection: undefined
    }
  }

  /**
   * Removes the current subscriptions and disconnects the specified
   * device.  It could be possible to maintain the connection across
   * the application, but for now the connection is within the context
   * of this screen.
   */
  async componentWillUnmount() {      
    Logger(`ConnectionScreen::componentWillUnmount state`, this.state);
    if (this.state.connection) {
      try {      
        Logger(`ConnectionScreen::componentWillUnmount attempting disconnect`);
        await RNBluetoothClassic.disconnectFromDevice(this.props.device.address);
      } catch (error) {
        // Unable to disconnect from device
      }    
    } 

    this.uninitializeRead();
  }

  /**
   * Attempts to connect to the provided device.  Once a connection is 
   * made the screen will either start listening or polling for 
   * data based on the configuration.
   */
  componentDidMount() {
    Logger(`ConnectionScreen::componentDidMount with state`, this.state);
    Logger(`ConnectionScreen::componentDidMount with props`, this.props);
    setTimeout(this.connect, 0);
  }

  connect = async () => {
    try {
      let connection = undefined;
      let connected = await RNBluetoothClassic.isDeviceConnected(this.props.device.address);      

      if (connected) {
        connection = await RNBluetoothClassic.getConnectedDevice(this.props.device.address);      

        Logger(`ConnectionScreen::connect received connected device`, connection);
        this.addData({
          data: `Re-established connection`,
          timestamp: new Date(),
          type: 'info'
        });
      } else {
        connection = await RNBluetoothClassic.connectToDevice(this.props.device.address, {});

        Logger(`ConnectionScreen::connect connected to device`, connection);
        this.addData({
          data: `Connection successful`,
          timestamp: new Date(),
          type: 'info'
        });
      }

      this.setState({connection});
      this.onConnection();
    } catch (error) {
      Logger(`ConnectionScreen::connect error`, error);
      this.addData({
        data: `Connection failed: ${error.message}`,
        timestamp: new Date(),
        type: 'error'
      });
    }
  }

  onConnection() {
    if (this.state.polling) {
      this.readInterval = setInterval((data) => onReceivedData(data), 300);
    } else {
      let eventType = `${BluetoothEvents.DEVICE_READ}@${this.props.device.address}`;
      this.readSubscription = RNBluetoothClassic.addListener(eventType, 
          (data) => this.onReceivedData(data));
    }
  }

  /**
   * Clear the reading functionality.
   */
  uninitializeRead(){
    if (this.readSubscription) {
      this.readSubscription.remove();
    }

    if (this.readInterval) {
      clearInterval(this.readInterval);
    }
  }

  /**
   * Handles the ReadEvent by adding a timestamp and applying it to 
   * list of received data.
   * 
   * @param {ReadEvent} event 
   */
  onReceivedData = (event) => {
    event.timestamp = new Date();
    this.addData({
      ...event,
      timestamp: new Date(),
      type: 'receive'
    });
  }

  addData = (message) => {
    this.setState({data: [message, ...this.state.data]});
  }

  /**
   * Attempts to send data to the connected Device.  The input text is
   * padded with a NEWLINE (which is required for most commands)
   */
  sendData = async () => {
    try {
      let message = this.state.text + '\r'; 
      await RNBluetoothClassic.write(this.props.device.address, message);
  
      this.addData({
        timestamp: new Date(),
        data: this.state.text,
        type: 'sent',
      });

      this.setState({ text: undefined });
    } catch(error) {
      console.log(error);
    }
  };

  render() {
    return (
      <Container>
        <Header>
          <Left>
            <Button transparent
                onPress={this.props.onBack}>
              <Icon type="Ionicons" name="arrow-back"/>
            </Button>          
          </Left>
          <Body>
            <Title>{this.props.device.name}</Title>
            <Subtitle>{this.props.device.address}</Subtitle>
          </Body>
        </Header>
        <View style={styles.connectionScreenWrapper}>
          <FlatList
            style={styles.connectionScreenOutput}
            contentContainerStyle={{justifyContent: 'flex-end'}}
            inverted
            ref="scannedDataList"
            data={this.state.data}
            keyExtractor={(item, index) => item.timestamp.toISOString()}
            renderItem={({item}) => (
              <View
                id={item.timestamp.toISOString()}
                style={{flexDirection: 'row', justifyContent: 'flex-start'}}>
                <Text>{item.timestamp.toLocaleDateString()}</Text>
                <Text>{item.type === 'sent' ? ' < ' : ' > '}</Text>
                <Text style={{flexShrink: 1}}>{item.data.trim()}</Text>
              </View>
            )}
          />
          <InputArea
            text={this.state.text}
            onChangeText={(text) => this.setState({text})}
            onSend={this.sendData}
            disabled={this.state.connection == undefined} />
        </View>        
      </Container>
    );
  }
}

const InputArea = ({text, onChangeText, onSend, disabled}) => {
  let style = disabled ? styles.inputArea : styles.inputAreaConnected;
  return (
    <View style={style}>
      <TextInput
        style={styles.inputAreaTextInput}
        placeholder={'Command/Text'}
        value={text}
        onChangeText={onChangeText}
        autoCapitalize="none"
        autoCorrect={false}
        onSubmitEditing={onSend}
        returnKeyType="send"
        disabled={disabled}
      />
      <TouchableOpacity
          style={styles.inputAreaSendButton}
          onPress={onSend}
          disabled={disabled}>
        <Text>Send</Text>
      </TouchableOpacity>
    </View>
  );
}

/**
 * TextInput and Button for sending
 */
const styles = StyleSheet.create({
  connectionScreenWrapper: {
    flex: 1
  },
  connectionScreenOutput: {
    flex: 1,
    paddingHorizontal: 8
  },
  inputArea: {
    flexDirection: "row",
    alignContent: "stretch",
    backgroundColor: "#ccc",
    paddingHorizontal: 16,
    paddingVertical: 6
  },
  inputAreaConnected: {
    flexDirection: "row",
    alignContent: "stretch",
    backgroundColor: "#90EE90",
    paddingHorizontal: 16,
    paddingVertical: 6
  },
  inputAreaTextInput: {
    flex: 1,
    height: 40
  },
  inputAreaSendButton: {
    justifyContent: "center",
    flexShrink: 1
  }
});