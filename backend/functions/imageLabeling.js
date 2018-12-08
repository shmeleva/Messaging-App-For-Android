const os = require('os');
const fs = require('fs');
const path = require('path');
const admin = require('firebase-admin');
const vision = require('@google-cloud/vision');
const functions = require('firebase-functions');
const spawn = require('child-process-promise').spawn;
const client = new vision.ImageAnnotatorClient();


exports.imageLabelingMessage = functions.database
    .ref('/chatMessages/{chat_id}/{message_id}')
    .onCreate((snapshot, context) => {
        const bucket = admin.storage().bucket()
        const createdData = snapshot.val()
        const bucketName = 'mcc-fall-2018-g08.appspot.com'
        const fileName = createdData.imageUrl

        if ( fileName ) {
            client
                .labelDetection(`gs://${bucketName}/${fileName}`)
                .then(results => {
                    const labels = results[0].labelAnnotations;
                    console.log('Labels:');
                    const label = labels[0].description
                    const chatId = context.params.chat_id
                    const messageId = context.params.message_id

                    console.log(results)
                    labels.forEach(label => console.log(label.description));
                    console.log(label)
                    console.log(chatId)
                    console.log(messageId)
                    console.log('!!!Update')

                    admin
                        .database()
                        .ref(`/chatMessages/${chatId}/${messageId}`)
                        .update({imageFeature: label}).then(result => {
                            console.log('Result')
                            console.log(result)
                        })
                        .catch(err =>{
                            console.log('Error')
                            console.log(err)
                        });
                    return true
                })
                .catch(err => {
                    console.error('ERROR:', err);
                    return false;
                });
        }
        return true;
})
