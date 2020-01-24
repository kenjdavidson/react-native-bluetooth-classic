---
chapter:
  title: API
  index: 5
title: Events
---

Native modules communicate by [sending events](https://facebook.github.io/react-native/docs/native-modules-ios#sending-events-to-javascript) to the Javascript NativeEventEmitter.  The standard events are substantially different between IOS and Android, I've done my best to replicate the IOS implementation on Android in order to take advantage of some of the better things that IOS does.   

With that said, there are still some things that don't really fit in well with the event structure of Bluetooth - mainly when taking into account multiple devices.  This will need to be planned on when moving forward:

- Should events be sent with the name `READ-BTDevice`, meaning that each `addListener` will be responsible for adding to `supportedEvents`.  If so, there is no way currently to remove specific events, only decrementing the total count - this will need to be addressed.
- Should the `RNBluetoothClassic` Javascript module control the propogation of the messages?  For example should the model get the `READ` event and based on the `BTDevice` should it forward to the appropriate emitter?  Need to look into how Javascript side emitters work when there are multple.

Please feel free to shoot me a message if you've got these answers!

There are a number of `BTEvents` available, across a number of categories while using the `RNBluetoothClassic` module.  They can be used for monitoring the environment, automatically connecting to devices or requested that device data is passed directly to your application.  In almost all cases you'll want to register these as such:

```javascript
componentDidMount() {
  this.onRead = RNBluetoothClassic.addListener(
    BTEvents.READ,
    this.handleRead,
    this,
  );
}

componentWillUnmount() {
  this.onRead.remove();
  RNBluetoothClassic.disconnect();
}
```

{:.warning}
> Always remembering to remove the subscription prior to your component unmounting.

