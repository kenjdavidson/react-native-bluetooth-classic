#!/bin/sh

#  protocolStrings.sh
#  BluetoothClassicExample
#
#  Created by Ken Davidson on 2019-06-18.
#  Copyright © 2019 Facebook. All rights reserved.

TARGET_PLIST="$BUILT_PRODUCTS_DIR/$INFOPLIST_PATH"
PROTOCOL_PLIST="$HOME/BluetoothClassicExample.plist"

if [ -f "$PROTOCOL_PLIST" ]
then
  echo "Merging protocols file: [$PROTOCOL_PLIST]"
  /usr/libexec/PlistBuddy -x -c "Print" "$PROTOCOL_PLIST"

  echo "/usr/libexec/PlistBuddy -c \"Merge $PROTCOL_PLIST\" $TARGET_PLIST"
  /usr/libexec/PlistBuddy -c "Merge $PROTOCOL_PLIST" "$TARGET_PLIST"
else
  echo "Protocols file [] was not found.  No protocols have been added to the example application."
  echo "Bluetooth devices will not be viewable from within IOS devices."
fi
