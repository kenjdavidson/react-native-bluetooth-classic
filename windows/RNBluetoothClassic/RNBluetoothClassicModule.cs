using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Bluetooth.Classic.RNBluetoothClassic
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNBluetoothClassicModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNBluetoothClassicModule"/>.
        /// </summary>
        internal RNBluetoothClassicModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNBluetoothClassic";
            }
        }
    }
}
