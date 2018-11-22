const admin = require('firebase-admin');
const moment = require('moment-timezone');
const _ = require('lodash');

const { getUser } = require('./models/user');
const { getChatWithExactMembers } = require('./models/chat');
const { errorMaker } = require('./lib/error-utils');

const db = admin.database();

const FUNCTION_NAME = 'createChat';
const NUM_MESSAGES = 10; // FIXME: Get it as an input instead

exports.handler = function (req, res) {
    const { members: userIds } = req.body;

    if (_.isEmpty(userIds)) {
        res.status(400).send('body.members is required');
        return;
    }

    const now = moment.utc();
    let members;
    let isGroupChat;
    let chatId;
    Promise.all(userIds.map(userId => getUser(userId)))
        .then((users) => {
            members = _.filter(users, user => !_.isNil(user));

            if (members.length !== userIds.length) {
                throw errorMaker('Some userIds are invalid', 400);
            }

            isGroupChat = members.length > 2;
            return isGroupChat ? null : getChatWithExactMembers(userIds, NUM_MESSAGES);
        })
        .then((chat) => {
            if (chat) {
                return res.json(chat);
            }

            return db.ref('chats').push()
                .then((newChat) => {
                    chatId = newChat.key;
                    return newChat.set({
                        id: chatId,
                        createdAt: now.toISOString(),
                        isGroupChat,
                        members: _.reduce(members, (output, { id }) => {
                            output[id] = true;
                            return output;
                        }, {}),
                    });
                })
                .then(() => {
                    const updates = _.reduce(members, (output, { id }) => {
                        output[`${id}/chats/${chatId}/joinedAt`] = now.toISOString();
                        return output;
                    }, {});
                    return db.ref('users').update(updates);
                })
                .then(() => {
                    return res.status(201).json({
                        id: chatId,
                        createdAt: now.toISOString(),
                        isGroupChat,
                        messages: [],
                        members: _.map(members, 'displayName'),
                    });
                });
        })
        .catch((err) => {
            console.error(`${FUNCTION_NAME} - Failed to create a chat:`, err.message);
            res.status(err.statusCode || 500).send(err.message);
        });
};
