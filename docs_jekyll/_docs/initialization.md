---
chapter:
  title: API
  index: 1
title: Initialization
---

React Native has built in support for [dependency injection](https://facebook.github.io/react-native/docs/native-modules-ios#dependency-injection) in order to provide native configuration options into your module.  At this point in time there aren't too many configuration options that you would pass in natively, but I can see there being a point when customization of the data processing goes in.  

Here are some examples of the native configuration available:

### Android 

To customize the `RNBluetoothClassic` module though package injection you can use any number of the `RNBluetoothClassicPackage` constructors when starting the application:

```java
List<ReactPackage> packages = new PackageList(this).getPackages();
packages.add(new RNBluetoothClassicPackage());
```

The available constructors are:

```java
public RNBluetoothClassicPackage(String delimiter, Charset charset)
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Default | Description|
| delimiter | string | `"\n"` | The delimiter to use while parsing individual messages. |
| encoding | BTCharsets | `Charset.forName("IOS_8859_1")` |  The native  [Charset](https://developer.android.com/reference/java/nio/charset/Charset).  This is the only method to set Encoding which does not match `CommonCharsets` IOS. |

```java
public RNBluetoothClassicPackage() {} // Uses defaults above
```

Down the road I'd like to customize the parsing so that messages are configurable, there's been a couple examples where a String isn't the preferred method for data - [https://github.com/kenjdavidson/react-native-bluetooth-classic/issues/1](https://github.com/kenjdavidson/react-native-bluetooth-classic/issues/1).

### IOS

I haven't been able to set this up as of yet - using React Native 0.59.9 without Pods - there were issues with linking and making the Swift project available to Objective C.  With the update to Pods in React Native 0.60.0 I'll try to get the configuration working, and an example posted when possible.