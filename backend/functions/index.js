const admin = require('firebase-admin');
const functions = require('firebase-functions');

// admin.initializeApp(functions.config().firebase);
admin.initializeApp({
  databaseURL: 'https://mcc-fall-2018-g08.firebaseio.com/',
  projectId: 'mcc-fall-2018-g08',
});

// const createChat = require('./create-chat');
// const getChats = require('./get-chats');
const sendNotification = require('./sendNotification');

// exports.createChat = functions.https.onRequest(createChat.handler);
// exports.getChats = functions.https.onRequest(getChats.handler);
exports.sendNotification = functions.database.ref('/tokens/{user_id}').onCreate((snapshot) => {
  console.log(JSON.stringify(snapshot))
  const response = admin.messaging().sendToDevice(
    ["fLhgsgxqGxw:APA91bHJ1ymaodEkvqe3AhbgufoxeqqiqQfyEOirkh-Ry1KFbqUHUgyVHxv5kupWMfshG2S4Qoq5BGvs6ml4E3tf2D0L_09EVKXwUr8JoK6ooTCqjhikX26rVswe62UESc3AZ3UCDXMM"], {
      notification: {
        title: 'Hello World',
        body: 'Body hello world'
      }
    }
  ).then((response) => {
    console.log(response)
    console.log(response.results[0]['error'])
    return true;
    // res.status(200).send('hello world token promise!');
  });
});
