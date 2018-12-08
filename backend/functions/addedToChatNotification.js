const admin = require('firebase-admin');
const database = admin.database();
const functions = require('firebase-functions');

exports.handler = functions.database.ref('/chats/{chat_id}/members/{user_id}').onCreate((snapshot, context) => {
  const tokenPromises = [];

  tokenPromises.push(admin.database()
    .ref(`/tokens/${context.params.user_id}`).once('value'));

  return Promise
    .all(tokenPromises)
    .then(tokens => {
      tokens.forEach(function(token) {
        var message = {
          notification: {
            title: "You were added to a group chat",
            body: ""
          },
          token: token.val()
        };
        response = admin.messaging().send(message)
          .then((response) => {
            return console.log('Successfully sent message:', response);
          })
          .catch((error) => {
            return console.log('Error sending message:', error);
          });
      });
      return true;
    });
});
