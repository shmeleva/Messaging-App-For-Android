const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp();

const resizeImage = require('./resizeImage')
const labelImage = require('./labelImage')
const notifyNewMessage = require('./notifyNewMessage');
const notifyNewGroupChat = require('./notifyNewGroupChat');

exports.labelImage = labelImage.imageLabelingMessage;
exports.resizeImage = resizeImage.changeImageSize;
exports.notifyNewMessage = notifyNewMessage.handler;
exports.notifyNewGroupChat = notifyNewGroupChat.handler;
