const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp(functions.config().firebase);

const createChat = require('./create-chat');
const getChats = require('./get-chats');

exports.createChat = functions.https.onRequest(createChat.handler);
exports.getChats = functions.https.onRequest(getChats.handler);
