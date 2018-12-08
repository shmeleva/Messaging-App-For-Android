# Frontend

# Backend

## Backend functions documentation

* **resizeImage** - a cloud function that is triggered every time when a new picture is uploaded to storage. It resizes the uploaded image into appropriate resolutions

* **labelImage** - a cloud function that is invoked when a new message added. It uses Google Cloud Vision API to label the image and then updates the field imageFeature with appropriate label

* **notifyNewChat** - a cloud function which is triggered when a new chat is created. It uses Cloud messaging API to send push notification to users.

* **notifyNewMessage** - a cloud function that notifies users by push notification that he or she has a new message.

* **notifyAddedToChat** - a cloud function that sends push notification when a new users is added to existing group chat

## Backend databases security rules

### The security rules are set accordingly

- users - only authorized users can read, update users collection and there is a validation for username to be unique

- usernames - only the owner can change the username

- tokens - owner can write a token to the tokens collection

- chats - if the chat hasn't been created yet, we allow read so there is a way to check this and create it; if it already exists, then authenticated user (specified by auth.id) must be in $key/members to write

- chatMessages - chatMessage can be read/written only by users who are in members list of the chat

