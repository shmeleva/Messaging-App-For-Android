const admin = require('firebase-admin');
const database = admin.database();
const functions = require('firebase-functions');

exports.handler = functions.database.ref('/chats/{chat_id}/members/{user_id}').onCreate((snapshot, context) => {
  const promises = [];

  promises.push(admin.database()
    .ref(`/chats/${context.params.chat_id}`).once('value'));

  promises.push(admin.database()
    .ref(`/tokens/${context.params.user_id}`).once('value'));

  return Promise
    .all(promises)
    .then(res => {
      var isGroupChat = res[0].val().isGroupChat;
      if (!isGroupChat)
        return;

      var message = {
        notification: {
          title: "You were added to a group chat",
          body: ""
        },
        token: res[1].val()
      };
      response = admin.messaging().send(message)
        .then((response) => {
          return console.log('Successfully sent message:', response);
        })
        .catch((error) => {
          return console.log('Error sending message:', error);
        });

      return true;
    });
});
