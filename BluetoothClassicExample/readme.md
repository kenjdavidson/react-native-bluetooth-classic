# BluetoothClassicExample

Example application built to test the installation and functionality of [react-native-bluetooth-classic](https://github.com/kenjdavidson/react-native-blutooth-classic) with [React Native 0.60.0](https://facebook.github.io/react-native/blog/2019/07/03/version-60).  There were a number of breaking changes and the standardization of Pod installs for IOS, this exmaple is to make sure that things go smoothly when using this functionality.

## Creation Process

The *BluetoothClassicExample* application was created following the example on the [Getting Started](https://facebook.github.io/react-native/docs/getting-started.html) documentation (react-native-cli on MacOS path).

#### Node & Watchman

`node` and `watchman` were already installed, but they were validated and updated using the specified commands:

    brew install node
    brew install watchman

#### XCode & Cocoapods

XCode was installed and updated already, but `pods` was required:

    sudo gem install cocoapods

#### Creating the Application

Since I wanted to start from scratch to ensure nothing was broken during a bad update, the old *BluetoothClassicExample* was deleted and a new project was created:

    rm -rf BluetoothClassicExample
    npx react-native init BluetoothClassicExample

Things seemed to go smoothly, there were no errors and the project structure was created as I'd expect.

## Install RNBluetoothClassic Library

Now we need to setup the library within the example application for development.  With the addition of [autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md) in v0.60.0 there are two options:

1. Adding the local library to the `react-native.config` file, information found [here](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md#user-content-how-can-i-autolink-a-local-library).  With that said, I have zero idea how this links the projects, and I'm not sure whether they are available for development afterwards (future Ken can sort that out).

2. Manually link the project, which is the choice I made, since it is still straight forwarda and allows for deveopment pods on IOS.

#### Javascript / Metro Config

Just like with the older version, in order to get Metro Packager to access the library we need to update the `metro.config.js` telling it where to look for `extraNodeModules`.  This is done by copying the following:

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

#### IOS / Development Pod

Following a couple docs and looking at the already created `Podspec` file for the React Native libraries:

```
pod 'React', :path => '../node_modules/react-native/'
pod 'React-Core', :path => '../node_modules/react-native/'
...
pod 'react-native-bluetooth-classic', :path => '../../'
```

There are two extra steps required here in order to get the `SWIFT_VERSION` set correctly:

1. Add a single Swift file `BluetoothClassicExample.swift` in this case.  I'm not sure if this attribute can be set without a Swift file in the project, but this seemed to work well enough - and allows for some customization if needed.

2. Add some `install` features to the `Podfile` to set the `SWIFT_VERSION` attribute.

```
Installing react-native-bluetooth-classic (0.10.3)
Generating Pods project
Integrating client project
Pod installation complete! There are 29 dependencies from the Podfile and 27 total pods installed.
```

Development can now be completed within XCode and the `BluetoothClassicExample` project using `Pods/Development Pods/react-native-bluetooth-classic`.

#### Android 

Without running `react-native link` since pretty much all the documentation says not to do it, I've manually added the project in all the places that it's expected in Android:

*settings.gradle*
```
rootProject.name = 'BluetoothClassicExample'
include ':react-native-bluetooth-classic'
project(':react-native-bluetooth-classic').projectDir = new File(rootProject.projectDir, '../../android')
```

*app/build.gradle*
```
dependencies {
    implementation project(':react-native-bluetooth-classic')
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "com.facebook.react:react-native:+"  // From node_modules
```

*MainApplication.java*
```
List<ReactPackage> packages = new PackageList(this).getPackages();
packages.add(new RNBluetoothClassicPackage());
// Packages that cannot be autolinked yet can be added manually here, for example:
// packages.add(new MyReactNativePackage());
```

#### Running the Application

This is where things got shady, in most cases you'd expect that the application would just start (following the docs) but this was not the case.  When attempting to run the application first:

    cd BluetoothClassicExample
    npx react-native run-ios

I received an error stating that `podfile.log` was not found, which mean that `pod install` was most likely not run.  This is pretty spot on, since it wasn't a step, continuing on I followed the directions:

    cd ios
    pod install

Things looked to be going along smoothly when nopes, I received the error:

> xcrun:_ error: SDK "iphoneos" cannot be located

After a quick Google someone smarter than I posted the solution [https://github.com/facebook/react-native/issues/18408#issuecomment-386696744](https://github.com/facebook/react-native/issues/18408https://github.com/facebook/react-native/issues/18408#issuecomment-386696744) which after doing was successful:

    sudo xcode-select --switch /Applications/Xcode.app
    pod install

Once this completed, things looked like we were good to go, going back to running the application:

    cd BluetoothClassicExample
    npx react-native run-ios

> info Found Xcode workspace "BluetoothClassicExample.xcworkspace" <br/>
> info Launching iPhone 11 (iOS 13.3) <br />
> info Building (using "xcodebuild -workspace BluetoothClassicExample.xcworkspace -configuration Debug -scheme BluetoothClassicExample -destination id=B4B3007B-D416-4922-B7C3-8677A29B7B94 -derivedDataPath build/BluetoothClassicExample")
................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................<br/>
> info Installing "build/BluetoothClassicExample/Build/Products/Debug-iphonesimulator/BluetoothClassicExample.app"<br/>
> info Launching "org.reactjs.native.example.BluetoothClassicExample"<br/>
> success Successfully launched the app on the simulator

which again looks like everything is great.  Switching over the the simulator we see what we're supposed to see (at least what's shown on the React Native docs) for an initial application.  Making a quick jump over to Android Studio and firing up an emulator confirms that:

    npx react-native run-android

is also successful!
