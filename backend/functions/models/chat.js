const admin = require('firebase-admin');
const _ = require('lodash');

const { getUser } = require('./user');

const db = admin.database();

/**
 * Get all or n latest messages in a chat
 * @param {string} chatId
 * @param {Array.<Object>} members
 * @param {number} numMessages - Number of latest messages to get
 * @returns {Promise.<Array>}
 */
function getChatMessages(chatId, members, numMessages = 0) {
    return new Promise((resolve, reject) => {
        function messageHandler(messagesSnapshot) {
            if (!messagesSnapshot.exists()) {
                resolve([]);
                return;
            }
    
            const messages = messagesSnapshot.val();
            const formattedMessages = _.chain(messages)
                .values()
                .map((message) => {
                    const sender = _.find(members, ['id', message.senderId]);
                    message.senderDisplayName = _.get(sender, 'displayName', '');
                    return message;
                })
                .value();
            resolve(formattedMessages);
        }

        const messagesRef = db.ref(`chatMessages/${chatId}`);

        if (numMessages > 0) {
            messagesRef.orderByKey().limitToLast(numMessages).once('value', messageHandler, err => reject(err));
        } else {
            messagesRef.orderByKey().once('value', messageHandler, err => reject(err));
        }
    });
}

/**
 * Get a chat with members and the latest 10 messages
 * @param {string} chatId
 * @param {number} numMessages
 * @returns {Promise.<Object|null>}
 */
function getChat(chatId, numMessages = 0) {
    return new Promise((resolve, reject) => {
        const chatRef = db.ref(`chats/${chatId}`);
        chatRef.once('value', (chatSnapshot) => {
            if (!chatSnapshot.exists()) {
                resolve(null);
                return;
            }

            const chat = chatSnapshot.val();

            const promises = _.chain(chat.members)
                .keys()
                .map(userId => getUser(userId))
                .value();
            Promise.all(promises)
                .then((members) => {
                    chat.members = _.chain(members)
                        .filter(member => !_.isNil(member))
                        .map('displayName')
                        .value();
                    return getChatMessages(chatId, members, numMessages);
                })
                .then((messages) => {
                    chat.messages = messages;
                    return resolve(chat);
                })
                .catch(err => reject(err));
        }, err => reject(err));
    });
}

function getChatMemberIds(chatId) {
    return new Promise((resolve, reject) => {
        const chatRef = db.ref(`chats/${chatId}`);
        chatRef.once('value', (chatSnapshot) => {
            if (!chatSnapshot.exists()) {
                reject(new Error('Chat not found'));
                return;
            }

            const chat = chatSnapshot.val();
            
            resolve({
                id: chatId,
                members: _.keys(chat.members),
            });
        }, err => reject(err));
    });
}

/**
 * Get a chat whose members match the given list
 * @param {Array.<string>} userIds
 * @param {number} numMessages
 * @returns {Promise.<Object>}
 */
function getChatWithExactMembers(userIds, numMessages = 0) {
    const sortedUserIds = userIds.sort();
    return new Promise((resolve, reject) => {
        const userRef = db.ref(`users/${userIds[0]}`);
        userRef.once('value', (userSnapshot) => {
            if (!userSnapshot.exists()) {
                reject(new Error('User not found'));
                return;
            }

            const user = userSnapshot.val();
            Promise.all(_.map(_.keys(user.chats), chatId => getChatMemberIds(chatId)))
                .then((chats) => {
                    let chatId;
                    for (let { id, members } of chats) {
                        if (_.isEqual(sortedUserIds, members.sort())) {
                            chatId = id;
                            break;
                        }
                    }

                    return chatId ? getChat(chatId, numMessages) : null;
                })
                .then(chat => resolve(chat))
                .catch(err => reject(err));
        }, err => reject(err))
    });
}

module.exports = {
    getChat,
    getChatWithExactMembers,
};
