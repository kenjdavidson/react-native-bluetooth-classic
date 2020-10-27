import React, {Component} from 'react';
import RNBluetoothClassic, { BluetoothEventType } from 'react-native-bluetooth-classic';
import { Container, Text, Header, Left, Button, Icon, Body, Title, Subtitle, Right } from 'native-base';
import { FlatList, View, StyleSheet, TextInput, TouchableOpacity, BackHandler } from 'react-native';

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
      connection: false,
      connectionOptions: {
        delimiter: '1'
      }
    }
  }

  /**
   * Removes the current subscriptions and disconnects the specified
   * device.  It could be possible to maintain the connection across
   * the application, but for now the connection is within the context
   * of this screen.
   */
  async componentWillUnmount() {      
    if (this.state.connection) {
      try {      
        await this.props.device.disconnect();
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
    setTimeout(() => this.connect(), 0);
  }

  async connect() {
    try {
      let connection = await this.props.device.isConnected();
      if (!connection) {
        connection = await this.props.device.connect(this.state.connectionOptions);

        this.addData({
          data: `Connection successful`,
          timestamp: new Date(),
          type: 'info'
        });
      }

      this.setState({connection});
      this.initializeRead();
    } catch (error) {
      this.addData({
        data: `Connection failed: ${error.message}`,
        timestamp: new Date(),
        type: 'error'
      });
    }
  }

  async disconnect() {
    try {
      let disconnected = await this.props.device.disconnect();
      
      this.addData({
        data: `Disconnected`,
        timestamp: new Date(),
        type: 'info'
      });

      this.setState({connection: !disconnected});
    } catch(error) {
      this.addData({
        data: `Disconnect failed: ${error.message}`,
        timestamp: new Date(),
        type: 'error'
      });
    }
  }

  initializeRead() {
    this.readSubscription = this.props.device.onDataReceived((data) => this.onReceivedData(data));
  }

  /**
   * Clear the reading functionality.
   */
  uninitializeRead(){
    if (this.readSubscription) {
      this.readSubscription.remove();
    }
  }

  /**
   * Handles the ReadEvent by adding a timestamp and applying it to 
   * list of received data.
   * 
   * @param {ReadEvent} event 
   */
  async onReceivedData(event) {
    event.timestamp = new Date();
    this.addData({
      ...event,
      timestamp: new Date(),
      type: 'receive'
    });
  }

  async addData(message) {
    this.setState({data: [message, ...this.state.data]});
  }

  /**
   * Attempts to send data to the connected Device.  The input text is
   * padded with a NEWLINE (which is required for most commands)
   */
  async sendData() {
    try {
      let message = this.state.text + '\r'; 
      await RNBluetoothClassic.writeToDevice(this.props.device.address, message);
  
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

  async toggleConnection() {
      if (this.state.connection) {
        this.disconnect();
      } else {
        this.connect();
      }
  }

  render() {
    let toggleIcon = this.state.connection ? 'radio-button-on' : 'radio-button-off';

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
          <Right>
            <Button transparent
              onPress={() => this.toggleConnection()}>
              <Icon type="Ionicons" name={toggleIcon}/>
            </Button>
          </Right>
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
            disabled={!this.state.connection} />
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