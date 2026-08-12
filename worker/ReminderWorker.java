package com.steadyme.app.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * WorkManager survives restarts and uses the OS scheduler for a daily check-in reminder.
 */
public class ReminderWorker extends Worker {
    public static final String CHANNEL_ID = "mood_reminders";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Mood reminders", NotificationManager.IMPORTANCE_DEFAULT));
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("How are you feeling?")
                .setContentText("Take a moment to log today's mood in SteadyMe.")
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1001, notification);
        return Result.success();
    }
}
