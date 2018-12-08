# Frontend

# Backend
## Function Deployment
1. Replace `PROJECT ID` in `/backend/.firebaserc` with the project ID.
2. Run
```
cd backend/functions
firebase login
npm run deploy
```

# Backend functions documentation

* **image_resizing** - a cloud function that is triggered every time when a new picture is uploaded to storage. It resizes the uploaded image into appropriate resolutions

* **imagelabeling** - a cloud function that is invoked when a new message added. It uses Google Cloud Vision API to label the image and then updates the field imageFeature with appropriate label

* **newChatNotification** - a cloud function which is triggered when a new chat is created. It uses Cloud messaging API to send push notification to users.

* **newMessageNotification** - a cloud function that notifies users by push notification that he or she has a new message.

* **addedToChatNotification** - a cloud function that sends push notification when a new users is added to existing group chat

