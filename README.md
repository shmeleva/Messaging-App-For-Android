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

![RegistrationActivity](https://lh3.googleusercontent.com/GcXhNskEyyF9jm1EaZzg58C3Pw9X-u_Z8ux40tKDVm-W26B3YMQg_Zc_dAwFY7U6e7REmEwAJeGJFhWFpg1B4H7oqIuqfO1xcicjgiQ2Cu7P4lZ1sYQH-_CWvvljvnwKYhCfkUwyf3FqHpqTzkfMi-a-w1SqsFRUKfWYYcSbEo92OtxO5lukSe1ULVGP8NaAogjyxfr2G57OyOtzGIocaCpZgM0EJmuWJu9pHmyY_MVp20yxx-46BB_Dm2AQGrKjNOvRmIC-ZCTZLf0Tqx75_QqKzzk57ktncAVUUH-iubTjNeHC64wenqykpQCGysgPZ-vWqwMBuFUfh_kp-vzlopFQFCiNTtLPf1CiQfZVNG4J1Ebn5OuttVljFDfBrY2dvUMW3Jo67B_5n3l34d2Gn7mp-fbfUWjBMtnn-daKLaYHQHHm5wEpTTYYT4_1OM9ZM8DxfAWdi55Sjx6NvbdMYZOesMMTw_wiLzZyYr9UURcv0XnjaBr2jykesyhhs9gB0b7H4BgoVS-aVNxqTZGo05lmzbDQjuE_Beqt9di-9yvW5e19Bd8svHAYwgF5K2uwpP-MGbXJKA8D3wqbtjwvJBqaWNY_Xz8giAjGmXMtRWBKGJSkh5Effm6GECbA9Y93nxuoXun2mlmOSEBRIS-JhwBQk9ODLnhp6-LL5Jwt8Hmra-NKS0C1jZtYBCJGm0Zc7nTt9ZKeMW1Flft2Dn3kcLzVMQ=w345-h689-no)

#### ChatListActivity

`ChatListActivity` is used for displaying user chats. It initially displays `ChatListFragment`.
In the Toolbar, there are Search and Settings action buttons that launch `SearchActivity` and `SettingsActivity` accordingly.

In the bottom right corner there is an expanding floating action button that is used for creating new chats; it also launches `SearchActivity`.

Chats are synchronized with Firebase Database with `ValueEventListener`s.

![ChatListActivity](https://lh3.googleusercontent.com/mOgr6cjhQhAv9CRNTsV2Rl0HQZb-HCgdxhOhNpQrYDB1g_f9pjot9HHJeueRpt0L-9IbTAksLhNUXarURiy01Tb2lKDZZ7k-DlTc-gTPdpC3hLtvwET_UedJHLXE6jAYBEmBIzUmE894Cqd5AAifAiKPJGYiMH3eNhtCUZahWxbH07HNIf16Z96BP8wOwBSZzg2LpeekeYm2UqaN0zte6SjuQUfWgiOh1y0ych68t_pWzlqIF6yG9smzwth_hfDww_B8uES8BXEOeV6Ri7ESFgSOpLyg6DqHD1b_hRTVt1ikHDRG2LkeaoPRes1Sd-KqTrsYgrEXjDuggPhGZXPeI8buCbEOaN9lH9wHRCBwTA6JoOtekrpCzwu28Z1DBk2X8_jGIqDabfENFzgnX3ANfP8Fwkn0DzWCX0OZHV3hj9iAWH9l3F0MjObSy7V_Nn-N6GIJbOODYW1SOvqvb0NNDMJVVodN5NGRfUpJaXPaKJXYuM2r9JVvX4kpm7xOckJjBCzd9kXAKzuS9UJZMdjlgCvhIBuhw5eAxaU4hEPXs6-RhQRvBCYhcvpRWnz5YZudzZ_0izaSz4gkM0Fd_uzz_gawbQigIa1gWtegj_66QXUKxB30B-lq6IAceP2WxFIdnAjvIVVrPRjq3yEUbNaQB9O5-UUcqIUnqjN9ED6Z165pmgvxuWnsvj-zC14WXq2IUhlr4uwdZWsG-gJhAnMvCmrq5Q=w345-h689-no)

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

![SettingsActivity](https://lh3.googleusercontent.com/u6JpyQzVR5ISXEay1lQhW6xtmUmbIbIbNKzKnWqPoRBznhR5yPxn_yA_x17Y9_ssYSY6x0w8RRFYjOxuloTGkRF-4Gv2h1qjgi_T4omAum9lFgxLNxSfg1NffRcSsfOP4ldWFP6UxUkC-PTabTygBrS09Ax-60mbTKJKK5rmSK8WrrJ2QnaKucoP6ocq0V2i5-Ap80drB5HGIToM_jXGUD3KcBBwte8H6A7H4_X1s2oSTbi81mnpW7mAk4bBw1ISW9YZMxvbtuQ1GZ2Zt3Km0BbF_3svUovq4mG9F7idWUHSrAJoNYSLEWh2k6KEpk1AaC42pxCgFoGuAR_jsTCzdAjD5gZBxgLz0rW7mQUD1KXYUFXiwI_rnaLYbemQsgcbCMRYxeysL3dotWN2FVg54VqZZo8eN6WDecRc8WiSjga3eee2W394ayBlxS3SV5g7ZlIbNW44IvpYqBYhKO17HSv9bVLBvjqsqS5Df6QcZwFuLmPdt-epX4QYZwcBg5N5_OEO80JiBeyU-Pj49wWTeuoEqLmCV2knPJdrygSDl_JgTNymB9ksgLZCF8AsWWghLXnIHBl5CYr5QU2Uu3UnLDTyMJgxYPEWmLyXLSarp8qWMKlXMXJ2fwoM9-47d4ZJ8p12fMp3EvsbHGdjaaf3f6G-FIJ2u-ml6-ux_NXNH-rLMzQwtSAMu1Cfhk0PtAZIOSFwFg59fy_NPng8yjmqHUGugw=w345-h689-no)

#### FullscreenImageActivity

`FullscreenImageActivity` is used for displaying a single picture that can be viewed in both portrait and landscape orientations, zoomed with a pinch-to-zoom gesture, downloaded in full resolution to the selected location, and shared through a third-party app.

#### BaseFragmentActivity

This class contains some methods that are used across multiple activities (e.g. adding a fragment or selecting/taking a picture).

### Fragments

#### ChatListFragment

`ChatListFragment` is used inside `ChatListActivity` (see _ChatListActivity_ section).

#### ChatFragment

`ChatListFragment` is used inside `ChatActivity`. It contains chat messages and allows to send messages and images. When a text message clicked, its content can be shared via third-party apps. When an image message clicked, the image is opened in `FullscreenImageActivity` (see _FullscreenImageActivity_ section). By clicking on the Toolbar, the user can open either private chat details or group chat details depending on the chat type.

![ChatFragment](https://lh3.googleusercontent.com/mmnCVjWUX1LxRDxJVhCyxj9UfUWxMNfG_mwMdbNg__to9LbfcXAMV-Ns3ULwhJSRzTXjx8wWW0pbj6wTc6RQZj7NEic63cjqmuqsNW62EvPp5JvAzqqmKueAqkonmy3M_TxgY0gLtFnBTrLbZdomQIyUA1T_vDuoFam1MwnvYaPWhznofVd-G0rOqAbE-IAiMrbZxQ-ba69dHa3L5jtLSwABG_SvmTbPn_x3ppKnGvQcyb8hcZrhtJbiSDuTw5gjvA_pfzoB7ggEJBaM3D4NogxGLivwf8JQDS8jHFeWE1RWq40lqtcZ1Q2qfGonrW7nb1hd_tReoMA30RWUDoVRGeM6PQiyVc_xs1chFkBOHAR8F8CT0mjRx3t5lTJefSuvvxD2ZMyRQd05gazZ7sjVIAT9GQCHtdcRGPI0oBCLZqSahNICe9ezhRaHLrk14kQgMmoaHJgYg8UZ-cTcnoXLqIkaCWnPIEVIfS0YiCQbBEYHi_eQ62027khvqBVpc3DD96fRWEpG3HCICBTZLDWTPwjkBeyX72otau4ROADocL8hdxImmBe0BIR-wW-hW71LUM9dII2UkD2-euhQ-Am42HH_h8h8-x8dJHj8SS7PDpjB5Nk_Rs6Dw2veirywhPctyzcK3t_ex-9NH41OpY2qTsUfl-SPzFzC2Hy3VPnLWicBgndcyuovxcFjuewOnvMTA9EmEjgxXOCay7rTDXQsbo4TWg=w345-h689-no)

#### PrivateChatSettingsFragment

`PrivateChatSettingsFragment` is used inside `ChatActivity`. It contains basic user information about the user such as their profile picture (or a placeholder if it is missing) and username. From this screen, the user can open the Gallery (only if the fragment was opened from the Chat screen), and either start a new chat or return to an existing chat depending.

#### GroupChatSettingsFragment

`GroupChatSettingsFragment` is used inside `ChatActivity`. It contains a list of chat members and allows to add new members to the chat (see `SearchActivity`), leave the chat, and open the Gallery.

![GroupChatSettingsFragment](https://lh3.googleusercontent.com/IUNsfUpflKfAqu9NPLqLWZ3dAi4a9oauJwcJyDaURd9G7uh7Uv9BJo91jouc-Q2GdGM_-mQYCd4tvEiML5QYjsWCrBfo10u1CjwGoSJPNLjXtUxbD3ukK9NQbxm17igjSZraAZmFqZOqxjsA1UUIbmqjSBXRNnpII2H_starVoRv4VgJPDAil3fpSHEDwvWHM947IMp8Z7Kyg5iABBha8iDTP9CyPiuKfaR1lL_nSiJ-U5teP8e5KUpDZ5eq9M5CvO1AwOU0QK9WhusrRqYb2Rk4nSIfVcGyuio_pQLWR6LTK4H1eZD6e9J34FeKPsJzW_SP9h2hbjNPCbXJtaDuyKVlNhrKchqoAJOF_kLqkiz6djz6y_iC5CvGVDVrnmtNmCB3-p-ft5lNCzjJGbx9aHBUN8kS0yJiFk-33U4Jvo7YlfkbqBdBG9S6doflK53Pt2gnpHRTzk3Y0Ig0IBZshcU04ubQfweLZiqazG0jNAIvd8FuMk-vEcGOGAeiYGyTLzDlWcc7nupynk5Uvmw8xBa8njtppbADpDK-C_BidgHV1oOCXGRlQSoBdqan9hb5HLkCW5BqcN60SbitRIHURZK4b1C_1tN7ExXdidw0WhWI_W6eBbKQaItdLVOTTBekeCDtXGnYyuhilv8v5SGoJegie3XBhD82v3k6G8makOYzaLkZj-zYQCqGr5cZQQ9Z7Knkaw67cyeb6W5qA6XCEhpb5Q=w345-h689-no)

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
