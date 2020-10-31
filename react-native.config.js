import { builtinModules } from "module";

/**
 * https://github.com/react-native-community/cli/blob/master/docs/platforms.md
 * 
 * Provides custom configuration to allow the RNBluetoothClassicModule to be autolinked
 * correctly.  For the purpose of configuration there is a Builder and a private
 * constructor that breaks default autoloading, for that reason the package and 
 * instance need to be manually provided.
 * 
 * If you wish to customize your DeviceConnections you will need to mark this 
 * dependancy as null in your own configuration and manually add the 
 * package to the list.
 * 
 * TODO add examples
 * 
 * @author kenjdavidson
 * 
 */
module.exports = {
  platforms: {
    /* The android configuration needs to supply a the package and default
     * instance.  The source and other keys will be created during the Gradle 
     * build.
     * 
     * https://github.com/react-native-community/cli/blob/master/packages/platform-android/native_modules.gradle
     */
    android: {
      packageImportPath: 'kjd.reactnative',
      packageInstance: 'RNBluetoothClassicPackage.DEFAULT_BUILDER.build()'
    }
  }
}