package com.example.koratuwa;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NotificationsActivity extends BaseActivity {

    private static final String CHANNEL_ID = "koratuwa_notifications";
    private static final String PREFS_NAME = "post_status_prefs";
    private static final String STATUS_MAP_KEY = "post_status_map";
    private static final String CLEARED_KEY = "cleared_notifications";
    private static final int REQUEST_NOTIFICATION_PERMISSION_CODE = 101;

    private ImageView ivBack;
    private LinearLayout notificationsContainer;
    private FirebaseFirestore db;
    private String currentUserId;

    private Map<String, String> postStatusMap = new HashMap<>();
    private Set<String> clearedNotifications = new HashSet<>();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ivBack = findViewById(R.id.back_arrow);
        notificationsContainer = findViewById(R.id.notificationsContainer);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadPostStatusMap();
        loadClearedNotifications();

        ivBack.setOnClickListener(v -> finish());

        createNotificationChannel();
        requestNotificationPermission();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new android.content.BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String postId = intent.getStringExtra("postId");
                        removeNotificationCard(postId);
                    }
                },
                new android.content.IntentFilter("REMOVE_NOTIFICATION_CARD")
        );

        listenForPostStatusChanges();
    }

    private void listenForPostStatusChanges() {
        db.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Notifications", "Listen failed.", e);
                        return;
                    }
                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String postId = dc.getDocument().getId();
                        String cropType = dc.getDocument().getString("cropType");
                        String status = dc.getDocument().getString("status");
                        if (status == null) continue;

                        String lastStatus = postStatusMap.get(postId);
                        boolean cleared = clearedNotifications.contains(postId);
                        String message = "Your post about " + cropType + " is " + status;

                        if (!cleared) {
                            addNotificationCard(new UserNotification(postId, message, "post_action", status, System.currentTimeMillis()));
                        }

                        if (!cleared && (lastStatus == null || !lastStatus.equals(status)) &&
                                ("approved".equals(status) || "rejected".equals(status))) {
                            showLocalNotification(new UserNotification(postId, message, "post_action", status, System.currentTimeMillis()));
                        }

                        postStatusMap.put(postId, status);
                        savePostStatusMap();
                    }
                });
    }

    private void addNotificationCard(UserNotification notification) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.notification_card_item, notificationsContainer, false);

        TextView tvNotificationText = cardView.findViewById(R.id.tvNotificationText);
        ImageView ivDismiss = cardView.findViewById(R.id.ivDismiss);

        tvNotificationText.setText(notification.getText());
        tvNotificationText.setTag(notification.getId());

        ivDismiss.setOnClickListener(v -> {
            notificationsContainer.removeView(cardView);
            NotificationManagerCompat.from(this).cancel(notification.getId().hashCode());

            clearedNotifications.add(notification.getId());
            saveClearedNotifications();
        });

        notificationsContainer.addView(cardView, 0);
    }

    private void removeNotificationCard(String postId) {
        for (int i = 0; i < notificationsContainer.getChildCount(); i++) {
            View card = notificationsContainer.getChildAt(i);
            TextView tvText = card.findViewById(R.id.tvNotificationText);
            if (tvText != null && postId.equals(tvText.getTag())) {
                notificationsContainer.removeView(card);
                clearedNotifications.add(postId);
                saveClearedNotifications();
                break;
            }
        }
    }

    private UserNotification pendingNotification = null;

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void showLocalNotification(UserNotification notification) {
        if (!checkNotificationPermission()) {
            // Save the notification to retry after permission is granted
            pendingNotification = notification;
            requestNotificationPermission();
            return;
        }

        // Clear previous pending notification
        pendingNotification = null;

        Intent intent = new Intent(this, NotificationDismissReceiver.class);
        intent.putExtra("postId", notification.getId());
        PendingIntent deleteIntent = PendingIntent.getBroadcast(
                this,
                notification.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Post Status Update")
                .setContentText(notification.getText())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDeleteIntent(deleteIntent);

        NotificationManagerCompat.from(this).notify(notification.getId().hashCode(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Koratuwa Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for post updates");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingNotification != null) {
                    // Retry showing notification after permission granted
                    showLocalNotification(pendingNotification);
                }
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPostStatusMap() {
        String json = sharedPreferences.getString(STATUS_MAP_KEY, null);
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                Iterator<String> keys = obj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    postStatusMap.put(key, obj.getString(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void savePostStatusMap() {
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, String> entry : postStatusMap.entrySet()) {
            try {
                obj.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferences.edit().putString(STATUS_MAP_KEY, obj.toString()).apply();
    }

    private void loadClearedNotifications() {
        clearedNotifications = sharedPreferences.getStringSet(CLEARED_KEY, new HashSet<>());
    }

    private void saveClearedNotifications() {
        sharedPreferences.edit().putStringSet(CLEARED_KEY, clearedNotifications).apply();
    }
}
