package com.example.pingmate;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class FCMNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String uid = remoteMessage.getData().get("uid");
            Intent intent = new Intent(this, homepage.class);
            intent.putExtra("uid", uid);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (remoteMessage.getNotification() != null) {
            // Optionally handle notification payload if needed
        }
    }
}


