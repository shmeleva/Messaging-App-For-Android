const functions = require('firebase-functions');

const admin = require('firebase-admin');
// admin.initializeApp();

const spawn = require('child-process-promise').spawn;

const path = require('path');
const os = require('os');
const fs = require('fs');


exports.changeImageSize = functions.runWith({memory: '2GB'}).storage.object().onFinalize( async (event) => {

    const bucket = admin.storage().bucket();
    const metadata = { contentType: event.contentType }
    const dirPath = path.dirname(event.name)

    if (path.basename(event.name).startsWith('resized-async-')) {
        console.log('We already renamed that file')
        return false;
    }

    const sizes = ['100x100', '400x400', '1500x1024']
    const resizePromisizes = sizes.map(async size => {
        const tmpFilePath = path.join(os.tmpdir(), path.basename(event.name));
        await bucket.file(event.name).download({destination: tmpFilePath})
        await spawn('convert', [tmpFilePath, '-resize', size, tmpFilePath])
        const filename = 'resized-async-' + size + path.basename(event.name)
        const savePath = path.join(dirPath, filename)
        await bucket.upload(tmpFilePath, {
            destination: savePath,
            metadata: metadata
        })

    })

    await Promise.all(resizePromisizes)
    return fs.unlinkSync(tmpFilePath)
})