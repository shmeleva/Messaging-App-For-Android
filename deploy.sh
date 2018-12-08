#!/bin/bash

# Android
cd frontend/android

echo "Building an APK..."
gradle assembleDebug

cd ../../../../../../../backend