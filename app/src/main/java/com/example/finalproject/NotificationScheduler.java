package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class NotificationScheduler {

    // Schedule a Notification
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleNotification(
            Context context,
            String title,
            String message,
            Calendar triggerTime,
            int notificationId) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Intent for the BroadcastReceiver
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("TITLE", title); // Pass the title
        intent.putExtra("MESSAGE", message); // Pass the message
        intent.putExtra("NOTIFICATION_ID", notificationId); // Pass a unique notification ID

        // Create a PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId, // Unique ID for this PendingIntent
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule the notification using AlarmManager
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, // Wake up device to trigger the alarm
                    triggerTime.getTimeInMillis(), // Time to trigger
                    pendingIntent // Intent to execute
            );
        }
    }

    // Example function to schedule a bill reminder
    private void scheduleBillReminder(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2024);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        NotificationScheduler.scheduleNotification(
                context,
                "Bill Reminder",
                "Your electricity bill is due today!",
                calendar,
                101 // Unique ID for the notification
        );
    }
}

