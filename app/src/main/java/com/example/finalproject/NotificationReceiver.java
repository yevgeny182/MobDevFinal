package com.example.finalproject;

import static com.example.finalproject.NotificationHelper.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("TITLE");
        String message = intent.getStringExtra("MESSAGE");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        NotificationHelper.createNotificationChannel(context); // Ensure channel exists
        sendNotification(context, title, message, notificationId);
    }

    // Send a Notification
    public static void sendNotification(Context context, String title, String message, int notificationId) {
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Set your app's notification icon
                .setContentTitle(title) // Title of the notification
                .setContentText(message) // Message displayed
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority level
                .setAutoCancel(true); // Dismiss notification on user tap

        // Notify the user
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public void scheduleNotification(Context context, String title, String message, Calendar triggerTime, int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("TITLE", title);
        intent.putExtra("MESSAGE", message);
        intent.putExtra("NOTIFICATION_ID", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
    }
}

