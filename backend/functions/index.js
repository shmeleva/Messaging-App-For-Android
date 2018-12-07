const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp({
  databaseURL: 'https://mcc-fall-2018-g08.firebaseio.com/',
  projectId: 'mcc-fall-2018-g08',
});

const newChatNotification = require('./newChatNotification');

exports.newChatNotification = newChatNotification.handler;
