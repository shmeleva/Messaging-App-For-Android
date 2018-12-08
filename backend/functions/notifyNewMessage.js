const admin = require('firebase-admin');
const database = admin.database();
const functions = require('firebase-functions');

exports.handler = functions.database.ref('/chatMessages/{chat_id}/{message_id}').onCreate((snapshot, context) => {
  const tokenPromises = [];

  tokenPromises.push(admin.database()
    .ref(`/users/${snapshot.val().senderId}`).once('value'));

  snapshot.val().receivers.forEach(function(element) {
    tokenPromises.push(admin.database()
      .ref(`/tokens/${element}`).once('value'));
  });

  return Promise
    .all(tokenPromises)
    .then(tokens => {
      var senderName = tokens[0].val().username;

      tokens.forEach(function(token, index) {
        if (index < 1) return;

        var message = {
          notification: {
            title: "New message",
            body: senderName + ": " + (snapshot.val().text === "" ? "📷" : snapshot.val().text)
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
