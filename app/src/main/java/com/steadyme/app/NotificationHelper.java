package com.steadyme.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.steadyme.app.data.FirebaseRepository;

public class NotificationHelper {
    public static final String CHANNEL_ID = "mood_alerts";

    public static void showNotification(Context context, String title, String message) {
        new FirebaseRepository().saveNotification(title, message);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Mood Alerts", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }
}
