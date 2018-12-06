const admin = require('firebase-admin');
const functions = require('firebase-functions');

// admin.initializeApp(functions.config().firebase);
admin.initializeApp({
  databaseURL: 'https://mcc-fall-2018-g08.firebaseio.com/',
  projectId: 'mcc-fall-2018-g08',
});

// const createChat = require('./create-chat');
// const getChats = require('./get-chats');
// const sendNotification = require('./sendNotification');

// exports.createChat = functions.https.onRequest(createChat.handler);
// exports.getChats = functions.https.onRequest(getChats.handler);
exports.sendNotification = functions.database.ref('/chats/{chat_id}').onCreate((snapshot) => {
  const tokenPromises = [];

  Object.keys(snapshot.val().members).forEach(function(element) {
    tokenPromises.push(admin.database()
      .ref(`/tokens/${element}`).once('value'));
  });


  // console.log("tokens: " + JSON.stringify(tokens));

  return Promise
    .all(tokenPromises)
    .then(tokens => {

      //
      let tokenStrings = tokens.map(x => JSON.stringify(x));
      console.log('!!!!!!!!!')
      console.log(JSON.stringify(tokens))
      // let tokens1 = stokens.map(x => x.value)

      // console.log("tokens: " + Object.keys(tokens.val()));
      // console.log("types: " + typeof Object.keys(tokens.val()))
      let token_list = [
        "cML34BO7utA:APA91bFNf5i3MO19wy7hR7Nz6KQUkjIIeHnmEseEoTBy4m4TIYeY7RMq3MbgWB-1btwlc6ziI2QAYuj5La5eQ3uM-OMhXn2FSNuWlp3sPskrtgh3k8ncqfAa9a5aUT3T-zO6p0TsXY0p-1btwlc6ziI2QAYuj5La5eQ3uM-OMhXn2FSNuWlp3sPskrtgh3k8ncqfAa9a5aUT3T-zO6p0TsXY0p",
        "fLhgsgxqGxw:APA91bHJ1ymaodEkvqe3AhbgufoxeqqiqQfyEOirkh-Ry1KFbqUHUgyVHxv5kupWMfshG2S4Qoq5BGvs6ml4E3tf2D0L_09EVKXwUr8JoK6ooTCqjhikX26rVswe62UESc3AZ3UCDXMM",
        "fQiL_yKVGIw:APA91bEy0BAaQ5sfRlssp2v-46evsGkiOBlzThVrXcrsmjLo-GumCdsl85QAAWzIk3lIYWlTArfUEueUGymeyxuW5WloackRYpzNgfX6_lNLwjqWEL8Y7uzK_f1kEs_z9AXhClrvBTze",
        "cuGQzmDWeac:APA91bGRXr5QVJ-ljlTjfaysdcUIhycR3srk6qZ7iYAI6g1ayHNNG2d3Z1T9aEWrTbWNxyooTsNsVdcmGMYoXJVP-KulzIGlKPvUeOnoBM9Kyi7H0K5dwhvnmICGh6QWaOyo-wDyjuWS",
        "cbyFPfbDQdM:APA91bF6jZi46zgUmBRqjnDGhb13c0sGyr9ZGwYfqj46537NEeES6--uD66IeBN7ZlX09X2mwrQxtr975lat6zLHv7paBKSK-1kwgQZEOgPc4LCbWKwAIDsFZec2OaxBfmHey6PJje5k"
      ];

      for (let elem of tokenStrings) {
        console.log("elem.key: " + elem);
        console.log("elem.key: " + typeof elem);
        // console.log("elem.key: " + elem.key)
        // console.log("elem.val(): " + elem.val())
        // console.log("types: " + typeof elem)
      }

      response = admin.messaging().sendToDevice(
        JSON.stringify(tokens), {
          notification: {
            title: "New chat was created",
            body: snapshot.val().lastMessage
          }
        }).then((result) => {
          console.log("@@@@@@@@@@")
          console.log(result)
          // console.log(result.results[0]['error'])
          for (let index in result.results) {
            console.log(result.results[index]['error'])
          }
          console.log("@@@@@@@")
          return true;
        });
      return true;
    });
});
