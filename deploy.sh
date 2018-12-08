#!/bin/bash

# Android
cd frontend/android
chmod 744 gradlew

echo "Building an APK..."
./gradlew assembleDebug

cd ../../backend