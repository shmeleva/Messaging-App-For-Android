#!/bin/bash

# Android
cd frontend/android
chmod 755 gradlew

echo "Building an APK..."
./gradlew assembleDebug

cd ../../backend/functions

npm install
firebase deploy
gcloud services enable vision.googleapis.com

