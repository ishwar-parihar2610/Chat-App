package com.example.chatapp.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.chatapp.R;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.model.user;
import com.example.chatapp.utilities.Constant;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.NotificationParams;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends  FirebaseMessagingService {

    private static final String TAG = "onMessageReceived";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken: "+token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("My Token", "onMessageReceived: " + remoteMessage.getNotification().getBody());

        user user=new user();
        user.id=remoteMessage.getData().get(Constant.KEY_USER_ID);
        user.name=remoteMessage.getData().get(Constant.KEY_NAME);
        user.token=remoteMessage.getData().get(Constant.KEY_FCM_TOKEN);

        int notificationId=new Random().nextInt();
        String channelId="chat_message";

        Intent intent=new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constant.KEY_USER,user);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,channelId);
        builder.setSmallIcon(R.drawable.ic_notifications);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(Constant.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(Constant.KEY_MESSAGE)));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence channelName="chat Message";
            String channelDescription="This Notification channel is used for that chat Message";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel=new NotificationChannel(channelId,channelName,importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId,builder.build());


    }
}
