const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp();

const newChatNotification = require('./newChatNotification');
const imageResizing = require('./image_resizing')
const imageLabeling = require('./imageLabeling')


exports.imageLabelingMessage = imageLabeling.imageLabelingMessage;
exports.imageResizing = imageResizing.changeImageSize;
exports.newChatNotification = newChatNotification.handler;
