const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp();

const imageResizing = require('./image_resizing')
const imageLabeling = require('./imageLabeling')
const newChatNotification = require('./newChatNotification');
const newMessageNotification = require('./newMessageNotification');
const addedToChatNotification = require('./addedToChatNotification');

exports.imageLabelingMessage = imageLabeling.imageLabelingMessage;
exports.imageResizing = imageResizing.changeImageSize;
exports.newChatNotification = newChatNotification.handler;
exports.newMessageNotification = newMessageNotification.handler;
exports.addedToChatNotification = addedToChatNotification.handler;
