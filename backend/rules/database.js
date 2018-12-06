{
  "rules": {
    "users": {
      "$id": {
        ".write": "$id === auth.uid",
        ".read": "auth != null",
        "username": {
          ".validate":
          	"!root.child('usernames').child(newData.val()).exists() ||
            	root.child('usernames').child(newData.val()).val() === auth.uid"
          }
      },
      ".indexOn": ["username"],

    },
    "usernames": {
      "$username": {
        ".write":
        	"root.child('usernames').child($username).val() === auth.uid
        		|| newData.val() === auth.uid",
        ".read": "auth != null"
      }
    },
    "tokens": {
      "$user_id": {
        ".write": "$user_id === auth.uid",
        ".read": "true"
      }
    }
  }
}