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

`SettingsActivity` allows the user to change their profile picture that can be either selected from the Gallery or taken with the Camera, update their username, change their image resolution preferences (these are device-specific and stored in shared preferences and log out from the app.

#### FullscreenImageActivity

`FullscreenImageActivity` is used for displaying a single picture that can be viewed in both portrait and landscape orientations, zoomed with a pinch-to-zoom gesture, downloaded in full resolution to the selected location, and shared through a third-party app.

#### BaseFragmentActivity

This class contains some methods that are used across multiple activities (e.g. adding a fragment or selecting/taking a picture).

## Resource Files



# Backend
## Function Deployment
1. Replace `PROJECT ID` in `/backend/.firebaserc` with the project ID.
2. Run
```
cd backend/functions
firebase login
npm run deploy
```
