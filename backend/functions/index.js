const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp(functions.config().firebase);

const getChats = require('./get-chats');

exports.getChats = functions.https.onRequest(getChats.handler);
