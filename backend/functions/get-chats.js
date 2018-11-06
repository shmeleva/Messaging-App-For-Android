const admin = require('firebase-admin');

const database = admin.database();

exports.handler = function (req, res) {
    res.send('Test');
}