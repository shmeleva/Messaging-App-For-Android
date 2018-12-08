{
  "rules": {
    "users": {
      "$id": {
        ".write": "auth != null",
        "username": {
          ".validate":
          	"!root.child('usernames').child(newData.val()).exists() ||
            	root.child('usernames').child(newData.val()).val() === auth.uid"
          }
      },
      ".indexOn": ["username"],
      ".read": "auth != null"
    },
    "usernames": {
      ".read": "auth != null",
      "$username": {
        ".write":
        	"root.child('usernames').child($username).val() === auth.uid
        		|| newData.val() === auth.uid"
      }
    },
    "tokens": {
      "$user_id": {
        ".write": "$user_id === auth.uid",
        ".read": "auth != null"
      }
    },
    "chats": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "chatMessages": {
      ".read": "auth != null",
      ".write": "auth != null"
		}
  }
}
