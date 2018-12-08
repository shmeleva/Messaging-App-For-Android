const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp();

const imageResizing = require('./resizeImage')
const imageLabeling = require('./labelImage')
const newChatNotification = require('./notifyNewChat');
const newMessageNotification = require('./notifyNewMessage');
const addedToChatNotification = require('./notifyAddedToChat');

exports.imageLabelingMessage = imageLabeling.imageLabelingMessage;
exports.imageResizing = imageResizing.changeImageSize;
exports.newChatNotification = newChatNotification.handler;
exports.newMessageNotification = newMessageNotification.handler;
exports.addedToChatNotification = addedToChatNotification.handler;
