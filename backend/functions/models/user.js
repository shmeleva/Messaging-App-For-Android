const admin = require('firebase-admin');

const db = admin.database();

function getUser(userId) {
    return new Promise((resolve, reject) => {
        const ref = db.ref(`users/${userId}`);
        ref.once('value', (snapshot) => {
            if (!snapshot.exists()) {
                resolve(null);
                return;
            }

            const user = snapshot.val();
            resolve(user);
        }, err => reject(err));
    });
}

module.exports = {
    getUser,
};
