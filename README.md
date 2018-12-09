# Building the Project

Run `./deploy.sh` from the root directory.
# Frontend

## Java & Kotlin Files

### Activities

#### SplashActivity

`SplashActivity` is used to display a splash screen while selection between `LoginActivity` for unauthorized users and `ChatListActivity` for authorized users takes place.

#### LoginActivity

`LoginActivity` is used for logging in with `email` and `password`; users who do not have an account launch `RegistrationActivity` from this screen. After successful authorization, users are redirected to `ChatListActivity`.

#### RegistrationActivity

`RegistrationActivity` is used for signing up with `username`, `email` and `password`. `username` is checked for uniqueness upon registration with Firebase Database rules. Additionally, users can optionally upload their profile picture that gets stored in Firebase Storage. After successful registration, users are redirected to `ChatListActivity`.

#### ChatListActivity

`ChatListActivity` is used for displaying user chats. It initially displays `ChatListFragment`.
In the Toolbar, there are Search and Settings action buttons that launch `SearchActivity` and `SettingsActivity` accordingly.

In the bottom right corner there is an expanding floating action button that is used for creating new chats; it also launches `SearchActivity`.

Chats are synchronized with Firebase Database with `ValueEventListener`s.

#### ChatActivity

`ChatActivity` can be populated with the following fragments:

* `ChatFragment`;
* `PrivateChatSettingsFragment` or `GroupChatSettingsFragment`;
* `GalleryFragment`.

#### SearchActivity

`SearchActivity` is used for searching users and adding them to the chat. It can be launched from four different locations and it's appearance and behaviour varies slightly depending on the caller:

* If it is called from the Toolbar, it is used as a searching tool; the user can see another user's profile before starting a chat;
* If it is called from the top FAB, it is used for creating a new private chat or going to an existing chat with a user.
* If it us called from the bottom FAB, it is used for creating a new group chat; multiple users can be selected.
* If it is called from the group chat settings screen, it is used for adding new users to the chat; multiple users can be added at once.

#### SettingsActivity

`SettingsActivity` allows the user to change their profile picture that can be either selected from the Gallery or taken with the Camera, update their username, change their image resolution preferences (these are device-specific and stored in shared preferences and log out from the app. Pictures are resized locally before upload (if either `low` or `high` resolution is selected) with Glide and then with Firebase Cloud Functions after upload so that other users can fetch pictures with a desired resolution.

#### FullscreenImageActivity

`FullscreenImageActivity` is used for displaying a single picture that can be viewed in both portrait and landscape orientations, zoomed with a pinch-to-zoom gesture, downloaded in full resolution to the selected location, and shared through a third-party app.

#### BaseFragmentActivity

This class contains some methods that are used across multiple activities (e.g. adding a fragment or selecting/taking a picture).

### Fragments

#### ChatListFragment

`ChatListFragment` is used inside `ChatListActivity` (see _ChatListActivity_ section).

#### ChatFragment

`ChatListFragment` is used inside `ChatActivity`. It contains chat messages and allows to send messages and images. When a text message clicked, its content can be shared via third-party apps. When an image message clicked, the image is opened in `FullscreenImageActivity` (see _FullscreenImageActivity_ section). By clicking on the Toolbar, the user can open either private chat details or group chat details depending on the chat type.

#### PrivateChatSettingsFragment

`PrivateChatSettingsFragment` is used inside `ChatActivity`. It contains basic user information about the user such as their profile picture (or a placeholder if it is missing) and username. From this screen, the user can open the Gallery (only if the fragment was opened from the Chat screen), and either start a new chat or return to an existing chat depending.

#### GroupChatSettingsFragment

`GroupChatSettingsFragment` is used inside `ChatActivity`. It contains a list of chat members and allows to add new members to the chat (see `SearchActivity`), leave the chat, and open the Gallery.

#### GalleryFragment

`GalleryFragment` is used to display the Gallery. Pictures can be grouped by date, sender, and feature.

#### SearchFragment

`SearchFragment` is used inside `SearchActivity` (see SearchActivity section).

### Views

#### AspectRatioImageView

`AspectRatioImageView` extends `ImageView` and displays pictures with a specified aspect ratio. It is used for displaying profile pictures on the Setting screen for adjusting a picture to always be square regardless of the screen width.

`WrapContentGridView` extends regular `GridView` and adjusts `GridView` height depending on the height of its content. It is used for displaying images in the Gallery.

### Utilities

`FirebaseAppGlideModule` is used to configure Glide to fetch images from Firebase Storage.

There are also other utilities used for getting and displaying pictures, preventing double-click events, formatting time, hiding a keyboard, etc.

### Models

There are three models: `User`, `Chat`, and `Message`. To some extent, they correspond to Firebase Database nodes.

## Resource Files

### Animations

`anim` contains animations for expanding and collapsing a FAB.

### Drawables

`drawable` contains backgrounds for some UI elements (e.g. chat messages, buttons) named as `bg_*` and vector icons named as `ic_*`. Icons are taken from https://material.io.

### Layouts

`layout` contains layouts for activities (`activity_*`), fragments (`fragment_*`), recycler view items (`item_*`), and dialogs.

### XML

`xml` contains a path configuration file for `FileProvider`.

# Backend

## Backend functions documentation

* `resizeImage` - a cloud function that is triggered every time when a new picture is uploaded to storage. It resizes the uploaded image into appropriate resolutions

* `labelImage` - a cloud function that is invoked when a new message added. It uses Google Cloud Vision API to label the image and then updates the field imageFeature with appropriate label

* `notifyNewGroupChat` - a cloud function which is triggered when the user is added to a group chat. It uses Cloud Messaging API to send push notification to users.

* `notifyNewMessage` - a cloud function that notifies users by push notification that he or she has a new message.

\*  **Note**: Push notifications are received only when the app is running in background.

## Backend databases security rules

### The security rules are set accordingly

- `users` - only authorized users can read, update users collection and there is a validation for username to be unique

- `usernames` - only the owner can change the username

- `tokens` - owner can write a token to the tokens collection

- `chats` - if the chat hasn't been created yet, we allow read so there is a way to check this and create it; if it already exists, then authenticated user (specified by auth.id) must be in $key/members to write

- `chatMessages` - chatMessage can be read/written only by users who are in members list of the chat

## Cloud Storage

Images are uploaded to */images* folder in cloud storage and only authorized users can access them.

## Database JSON Tree

```json
{
  "chatMessages" : {
    "<chatId>" : {
      "<messageId>" : {
        "id" : "<messageId>",
        "imageUrl" : "<string>",
        "senderId" : "<string>",
        "text" : "<string>",
        "timestamp" : <timestamp>
      }
    }
  },
  "chats" : {
    "<chatId>" : {
      "id" : "<chatId>",
      "isGroupChat" : <boolean>,
      "lastMessage" : "<string>",
      "members" : {
        "<userId>" : true,
        "<userId>" : true
      },
      "updatedAt" : <timestamp>
    },
  },
  "tokens" : {
    "<userId>" : "<token>",
  },
  "usernames" : {
    "<username>" : "<userId>",
  },
  "users" : {
    "<userId>" : {
      "id" : "<userId>",
      "lowercaseUsername" : "<string>",
      "profilePicUrl" : "<string>",
      "username" : "<string>",
      "chats" : {
        "<chatId>" : {
          "joinedAt" : <timestamp>
        }
      }
    }
  }
}
```
