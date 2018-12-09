#!/bin/bash

# Android
cd frontend/android
chmod 755 gradlew

echo "Building an APK..."
./gradlew assembleDebug

cd ../../backend/functions

echo "Running npm install"
npm install

echo "Deploying Firebase"
firebase deploy

echo "Enabling vision.googleapis.com"
gcloud services enable vision.googleapis.com
