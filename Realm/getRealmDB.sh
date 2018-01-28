#!/bin/bash

osascript -e 'quit app "Realm Browser"'

ADB_PATH="/Users/doneu/Library/Android/sdk/platform-tools"
PACKAGE_NAME="com.doneu.backgammoncounter"
DB_NAME="default.realm"
DESTINATION_PATH="/Users/doneu/AndroidStudioProjects/BackgammonCounter/Realm/${DB_NAME}"
NOT_PRESENT="List of devices attached"
ADB_FOUND=`${ADB_PATH}/adb devices | tail -2 | head -1 | cut -f 1 | sed 's/ *$//g'`
if [[ ${ADB_FOUND} == ${NOT_PRESENT} ]]; then
    echo "Make sure a device is connected"
else
    ${ADB_PATH}/adb exec-out run-as ${PACKAGE_NAME} cat files/${DB_NAME} > ${DESTINATION_PATH}
    echo "Database exported to ${DESTINATION_PATH}"
    open default.realm
fi