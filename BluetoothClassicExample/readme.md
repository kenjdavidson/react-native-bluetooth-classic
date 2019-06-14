# BluetoothClassicExample

Testing application for react-native-bluetooth-classic module.

## Setup

When setting up the example application, there are a number of items that might need to be managed manually:

1. When attempting to run, you'll need to double check that the module and app are configured for your version of Android Studio.  In my case when first setting these up, I was running into an issue where I only had API 28 available, but the `react-native-library` and `react-native` commands provided separate versions:

  - Firstly the module project was defaulting to 0.20.0 when attempting to build, this caused the app to throw errors stating that the Module didn't override the correct methods.  To resovle this, I had to install the peer-dependancies locally so that I could update the module project and use the correct version of React Native that I was expecting (0.59.9).
  - https://github.com/spatie/npm-install-peers#readme

2. The second issue was that NPM 5+ performs symlinking of local projects, for that reason you'll see something that looks like this:
  ```
  $ ls -l node_modules/react-native-bluetooth-classic
  lrwxr-xr-x  1 user  group  5 13 Jun 12:46 node_modules/react-native-bluetooth-classic -> ../..
  ```

  to correct this use the following:
  - https://www.npmjs.com/package/install-local

3. 

## Running

Once configured, the application can be run from a number of methods:

1. Through VSCode using the commands `Apple/shift/P React-Native: Run Android on Emulator`

2. Through Android Studio clicking the `run` button

## Usage

The application contains two screens:

### Device List

Displays a list of devices currently connected.  Clicking on a device will bring you to the device connection screen where messages can be sent and received.

### Device Connection

Displays a text area and text field allowing you to send and monitor communication.  