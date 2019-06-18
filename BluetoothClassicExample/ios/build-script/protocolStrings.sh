#!/bin/sh

#  protocolStrings.sh
#  BluetoothClassicExample
#
#  Created by Ken Davidson on 2019-06-18.
#  Copyright © 2019 Facebook. All rights reserved.

INFO_PLIST="$BUILT_PRODUCTS_DIR/$INFOPLIST_PATH"
PROTOCOL_PLIST="/Users/kendavidson/BluetoothClassicExample.plist"

/usr/libexec/PlistBuddy -c "Merge $PROTOCOL_PLIST" "$INFO_PLIST"
