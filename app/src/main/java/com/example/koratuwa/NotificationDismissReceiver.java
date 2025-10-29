package com.example.koratuwa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationDismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String postId = intent.getStringExtra("postId");

        // Remove in-app card if user swipes system notification
        Intent removeIntent = new Intent("REMOVE_NOTIFICATION_CARD");
        removeIntent.putExtra("postId", postId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(removeIntent);
    }
}
