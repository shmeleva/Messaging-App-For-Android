#!/bin/bash

# Android
cd frontend/android

echo "Building an APK..."
gradle assembleDebug

cd app/build/outputs/apk/debug

echo "Installing the APK on the device..."
adb install app-debug.apk

echo "App installation done!"

cd ../../../../../../../backend