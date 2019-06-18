# BluetoothClassicExample

Testing application for react-native-bluetooth-classic module.

## Setup

When attempting to setup and configure the local project, there are a couple possible issues that may be run into.  I've documented all the pain points I had while configuring the BluetoothClassicExample so that if they occur you will be able to resolve them and continue testing.

### Android 
The version of Android SDK which was created when using `react-native-library` and `react-native init` were different. 

  - Firstly the module project was defaulting to 0.20.0 when attempting to build, this caused the app to throw errors stating that the Module didn't override the correct methods.  To resovle this, I had to install the peer-dependancies locally so that I could update the module project and use the correct version of React Native that I was expecting (0.59.9) (npm-install-peers#readme)[https://github.com/spatie/]
  - Edit - since resolving the second issue I no longer needed peer dependencies as my project was linked locally and could therefore be run.  The only issue was that I had to do development through the BluetoothClassicExample imported library, instead of on it's project workspace (as the two react-native versions still conflicted so editing directly caused 0.20.0 to be used and caused errors.)

2. The second issue was that NPM 5+ performs symlinking of local projects, for that reason you'll see something that looks like this:
  ```
  $ ls -l node_modules/react-native-bluetooth-classic
  lrwxr-xr-x  1 user  group  5 13 Jun 12:46 node_modules/react-native-bluetooth-classic -> ../..
  ```
  to correct this use the following (install-local)[https://www.npmjs.com/package/install-local].  Sadly after doing so I ran into some other issues, which caused some problems.  It made the build process intolerable since I had to continually `install-local` when changing the library code.

3. When installing locally you may run into issues with NPM > 5 where symlink are created.  After googling a bunch it seems like this is a pretty well known 'issue'.  Thanks to a posting [by a smarter person than I](https://github.com/facebook/metro/issues/1#issuecomment-501143843) this has been resolved by updating the `metro.config.js` file in the application project to the following:

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

### IOS

IOS requires that UISupportedExternalAccessoryProtocols are configured within the Info.plist file.  As per the MFi these values should not be made available or stored within git unless working with the specific vendor.  The IOS build has been updated to include a merging of during the Run Scripts phase:

Info.plist 
~/BluetoothClassicExample.plist

You're responsible for creating this file:

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>UISupportedExternalAccessoryProtocols</key>
  <array>
    <string>com.allflex-europe.gprplus_110</string>
  </array>
</dict>
</plist>
```

## Running Application

### Android
Once configured, the application can be run from a number of methods:

1. Through VSCode using the commands 
```
Apple/Shift/P 
React-Native: Run Android on Emulator
```

2. Through Android Studio clicking the `run` button

### IOS

In order to debug remotely (live device) the remote ip needs to be updated, as per the documentation for (React Native Debugging)[https://facebook.github.io/react-native/docs/debugging] do the following:

- On iOS devices, open the file RCTWebSocketExecutor.m and change "localhost" to the IP address of your computer, then select "Debug JS Remotely" from the Developer Menu.

or 

- Select "Dev Settings" from the Developer Menu, then update the "Debug server host for device" setting to match the IP address of your computer.
 
## Usage

The application contains two screens:

### Device List

Displays a list of devices currently connected.  Clicking on a device will bring you to the device connection screen where messages can be sent and received.

### Device Connection

Displays a text area and text field allowing you to send and monitor communication.  Scanned data will be displayed on the screen with the timestamp.

TODO - add support for sending data to a device