
# react-native-bluetooth-classic

React Native Bluetooth Classic functionality for both Android and IOS.  

Based off the [react-native-bluetooth-serial](https://github.com/rusel1989/react-native-bluetooth-serial) port, and updated to replace [CoreBluetooth](https://developer.apple.com/documentation/corebluetooth) (BLE) on IOS with [External Accessory](https://developer.apple.com/documentation/externalaccessory/eaaccessory).

The purpose of this was three fold:
- Take a run at learning how to work with React Native modules, I find the best way to learn is to take an established project and deconstruct/construct.
- The project I was working on required that both Android and IOS supported a Bluetooth Classic serial scanner.
- Seemed like a good idea.

## Getting started

### Install Local

```
$ git clone kenjdavidson/react-native-bluetooth-classic
$ cd MyReactNativeProject
$ npm install ../react-native-bluetooth-classic --save
```

When installing locally you may run into issues with NPM >5 where symlink are created.  After googling a bunch it seems like this is a pretty well known 'issue'.  Thanks to a posting [by a smarter person than I](https://github.com/facebook/metro/issues/1#issuecomment-501143843) this has been resolved by updating the `metro.config.js` file in the application project to the following:

```
let path = require('path');
module.exports = {
    transformer: {
        getTransformOptions: async () => ({
            transform: {
                experimentalImportSupport: false,
                inlineRequires: false
            }
        })
    },
    resolver: {
        /* This configuration allows you to build React-Native modules and
         * test them without having to publish the module. Any exports provided
         * by your source should be added to the "target" parameter. Any import
         * not matched by a key in target will have to be located in the embedded
         * app's node_modules directory.
         */
        extraNodeModules: new Proxy(
            /* The first argument to the Proxy constructor is passed as 
             * "target" to the "get" method below.
             * Put the names of the libraries included in your reusable
             * module as they would be imported when the module is actually used.
             */
            {
                'react-native-bluetooth-classic': path.resolve(__dirname, '../')
            },
            {
                get: (target, name) =>
                {
                    if (target.hasOwnProperty(name))
                    {
                        return target[name];
                    }
                    return path.join(process.cwd(), `node_modules/${name}`);
                }
            }
        )
    },
    projectRoot: path.resolve(__dirname),
    watchFolders: [
        path.resolve(__dirname, '../')
    ]
};
```

### Install from NPM (may not be complete yet)
```
$ npm install react-native-bluetooth-classic --save
```

### Automatic installation

I was able to link the project with the BluetoothClassicExample project using the following command (see the local NPM install above).  

`$ react-native link react-native-bluetooth-classic`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-bluetooth-classic` and add `RNBluetoothClassic.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBluetoothClassic.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNBluetoothClassicPackage;` to the imports at the top of the file
  - Add `new RNBluetoothClassicPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-bluetooth-classic'
  	project(':react-native-bluetooth-classic').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-bluetooth-classic/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-bluetooth-classic')
  	```

##### Annoyances

When first building the Android project there were issues with react-native-create-library and the version of Android/Gradle installed on my machine.  This needed to be resolved by ensuring that the project was inline with the version of Android Studio and the Android plugin for gradle.  In my case, the project was configured with 1.3.1 and 2.2, which caused problems, in order to resolve [Android plugin for gradle versions](https://developer.android.com/studio/releases/gradle-plugin.html)

1. Updated `gradle-wrapper.properties` to modify the line:
`distributionUrl=https\://services.gradle.org/distributions/gradle-5.1.1-all.zip`

2. Updated `build.gradle` to ensure the buildscript section matched the following:
  - Added google() to buildscript
  - added jcenter() to repositories

```
buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:3.4.0'
    }
}

repositories {
    mavenCentral()
    google()
    jcenter()
}
```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNBluetoothClassic.sln` in `node_modules/react-native-bluetooth-classic/windows/RNBluetoothClassic.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Bluetooth.Classic.RNBluetoothClassic;` to the usings at the top of the file
  - Add `new RNBluetoothClassicPackage()` to the `List<IReactPackage>` returned by the `Packages` method

## Example

The BluetoothClassicExample is included, it supports the following features (so far):

- Listing the paired devices (Android)
- Connecting to a selected device
- Displaying any information from the device serial

TODO - Add in writing from app to device example.

## Usage

Import the module using the following:

```javascript
import RNBluetoothClassic, {BTEvents} from 'react-native-bluetooth-classic';
```

In all cases the following API/Events are available within Javascript for both Android and IOS (no code splitting) if there are any native calls that are not available on the native side, the promise will be rejected with an appropriate message (kind of like UnssupportedOperationException since I'm used to Java) - I found this important as I see no point in duplicating code as the whole purpose of React Native was for me not to.

### API

The following API is available on both Android and IOS (unless specifically stated not).  I've done my best to duplciate all the methods available on both, so there should be no need to use Platform or file switching.  For any API calls that aren't supported on a specific environment, they should reject the promise with an 'UnsupportedOperation' error.

#### Promise requestEnabled()

#### Promise isEnabled()

#### Promise list()

#### Promise discoverUnpairedDevices()

#### Promise cancelDiscovery() 

#### Promise pairDevice(String id) 

#### Promise unpairDevice(String id)

#### Promise connect(String id)

#### Promise disconnect()

#### Promise isConnected()

#### Promise getConnectedDevice()

#### Promise writeToDevice(String message)

#### Promise readFromDevice()

#### Promise readUntilDelimiter(String delimiter)

#### Promise setDelimiter(String delimiter)

#### Promise available()

### Events
  
