package com.steadyme.app.worker;
import android.app.*; import android.content.*; import androidx.annotation.NonNull; import androidx.core.app.NotificationCompat; import androidx.work.*;
/** WorkManager survives restarts and uses the OS scheduler for a daily check-in reminder. */
public class ReminderWorker extends Worker {
 public static final String CHANNEL_ID="mood_reminders";
 public ReminderWorker(@NonNull Context c,@NonNull WorkerParameters p){super(c,p);}
 @NonNull public Result doWork(){ NotificationManager nm=getApplicationContext().getSystemService(NotificationManager.class); if(android.os.Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID,"Mood reminders",NotificationManager.IMPORTANCE_DEFAULT)); Notification n=new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("How are you feeling?").setContentText("Take a moment to log today's mood in SteadyMe.").setAutoCancel(true).build(); nm.notify(1001,n); return Result.success(); }
}
