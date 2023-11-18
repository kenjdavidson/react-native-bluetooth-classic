---
title: RNBluetoothClassicPackage
description: "Provides customziation of the RNBluetoothClassicModule"
---

Within the React Native [Android Native Modules](https://reactnative.dev/docs/native-modules-android) the implemented `ReactPackage`(s) are used to configure/customize the module implementation.

## Implementing the Package

### Pre-0.60.0

Prior to 0.60.0 your application needed to be linked manually with the React Native Bluetooth Classic library.  This was a pretty straight forward process that involved adding the package to the `getPackages()` method:

```java
@Override
protected List<ReactPackage> getPackages() {
  @SuppressWarnings("UnnecessaryLocalVariable")
  List<ReactPackage> packages = new PackageList(this).getPackages();
  packages.add(new RNBluetoothClassicPackage());
  return packages;
}
```

to your `MainApplication` code.

### Post-0.60.0

Moving forward with 0.60.0 (and higher) React Native introduced [autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md) which essentially just adds the dependency packages that it finds to the `PackageList` generated class.  

Nothing more than saving `react-native-bluetooth-classic` to your dependencies is required.  Effectively this does the exact same thing, just during the build process:

```java
  public ArrayList<ReactPackage> getPackages() {
    return new ArrayList<>(Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new ReactToolbarPackage(),
      new RNBluetoothClassicPackage()
    ));
  }
```

## Customizing your Package

The standard package provides the basic:

- Rfcomm Acceptor
- Rfcomm Connector
- String delimited connection

which should be usable for most projects.  At times though, it may be required to add/customize the configuration available to your React Native application.  To do this the following steps are required:

The first step is to either: [disabling autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md#how-can-i-disable-autolinking-for-unsupported-library) or [customize autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md#how-can-i-disable-autolinking-for-unsupported-library) within your application.

### Disable Autolinking

If you choose to disable autolinking, you will need to add the following:

```javascript
// react-native.config.js
module.exports = {
  dependencies: {
    'react-native-bluetooth-classic': {
      platforms: {
        android: null, // disable Android platform, other platforms will still autolink if provided
      },
    },
  },
};
```

and then manually link the application following the steps in [pre 0.60.0](#pre-0600), by adding the configuration (below) to the `MainApplication`.

### Customize Autolinking

Android gives a slick way of customizing/providing your customized package into the `PackageList` generated class, by using the [platform dependency config](https://github.com/react-native-community/cli/blob/master/docs/platforms.md#dependencyconfig) in order to tell your application just how to do it.

First, you'll need to create your custom package/provider:

```java
package my.custom.bluetooth;

public static class MyCustomBluetoothPackager {
  public static final PACKAGE = RNBluetoothClassicPackage.builder()
    .withConnectionFactory("myconnection", MyDeviceConnection::new)
    .build();
}
```

You then need to add this configuration to the `dependency`:

```javascript
// react-native.config.js
module.exports = {
  dependencies: {
    'react-native-bluetooth-classic': {
      platforms: {
        android: {
          packageImportPath: 'import my.custom.bluetooth.MyCustomBluetoothPackager;',
          packageInstance: 'MyCustomBluetoothPackager.PACKAGE'
        },
      },
    },
  },
};
```

Once this is completed, your `PackageList` will be generated as such:

```java
// PackageList

  public ArrayList<ReactPackage> getPackages() {
    return new ArrayList<>(Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new ReactToolbarPackage(),
      MyCustomBluetoothPackager.PACKAGE
    ));
  }
```

> Or you can build your package like any other while providing `new MyCustomBluetoothPackage()` for the `packageInstance` value.
