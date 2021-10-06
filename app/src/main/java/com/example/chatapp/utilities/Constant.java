package com.example.chatapp.utilities;

import java.util.HashMap;

public class Constant {
    public static final String KEY_COLLECTION_USERS="users";
    public static final String KEY_NAME="name";
    public static final String KEY_EMAIL="email";
    public static final String KEY_PASSWORD="password";
    public static final String KEY_PREFERENCE="chatAppPreference";
    public static final String KEY_IS_SIGNED_IN="isSignIn";
    public static final String KEY_USER_ID="userId";
    public static final String KEY_IMAGE="image";
    public static final String KEY_FCM_TOKEN="fcmToken";
    public static final String KEY_USER="user";
    public static final String KEY_COLLECTION_CHAT="chat";
    public static final String KEY_SENDER_ID="senderId";
    public static final String KEY_RECEIVER_ID="receiverID";
    public static final String KEY_MESSAGE="message";
    public static final String KEY_TIMESTAMP="timeStamp";
    public static final String KEY_AVAILABILITY="availability";
    public static final String REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content-type";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS="registration_ids";



    public static HashMap<String ,String > remoteMsgHeaders=null;
    public static HashMap<String ,String > getRemoteMsgHeaders(){
       if (remoteMsgHeaders==null){
           remoteMsgHeaders=new HashMap<>();
           remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,"Key=AAAAP7uQIyg:APA91bEyv2V28qhUMZd7Hv99iHRzUgHDvklJapMZiz7dlJ5YgC6lyvjo5JY14xlXoND2r3hdVylxuhgKLSLBlrlGf3WweuvY0vQDa6n_pzkpjkdOZqx3bir5JRiHkWjTd4W6ibfxDbUa");
           remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,"application/json");

       }
       return remoteMsgHeaders;
    };












}
