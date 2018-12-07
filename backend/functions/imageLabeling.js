const os = require('os');
const fs = require('fs');
const path = require('path');
const admin = require('firebase-admin');
const vision = require('@google-cloud/vision');
const functions = require('firebase-functions');
const spawn = require('child-process-promise').spawn;
const client = new vision.ImageAnnotatorClient();


exports.imageLabeling = functions.runWith({memory: '2GB'}).storage.object().onFinalize( async (event) => {
    const bucket = admin.storage().bucket();
    const metadata = { contentType: event.contentType }
    const dirPath = path.dirname(event.name)
    const bucketName = event.bucket;
    const fileName = event.name;

    if (path.basename(event.name).startsWith('resized-async-')) {
        return false;
    }
    console.log('On finalize called!!!!');
    console.log(event.name)
    console.log(bucketName)

    client
        .labelDetection(`gs://${bucketName}/${fileName}`)
        .then(results => {
            const labels = results[0].labelAnnotations;
            console.log('Labels:');
            labels.forEach(label => console.log(label.description));
        })
        .catch(err => {
            console.error('ERROR:', err);
        });

    return true;
})
