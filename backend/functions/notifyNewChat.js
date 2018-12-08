const admin = require('firebase-admin');
const database = admin.database();
const functions = require('firebase-functions');

exports.handler = functions.database.ref('/chats/{chat_id}').onCreate((snapshot) => {
  const tokenPromises = [];

  Object.keys(snapshot.val().members).forEach(function(element) {
    tokenPromises.push(admin.database()
      .ref(`/tokens/${element}`).once('value'));
  });

  return Promise
    .all(tokenPromises)
    .then(tokens => {
      tokens.forEach(function(token) {
        var message = {
          notification: {
            title: "New chat was created",
            body: snapshot.val().lastMessage
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
