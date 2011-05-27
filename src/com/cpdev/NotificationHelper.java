package com.cpdev;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public abstract class NotificationHelper {

    public static final int NOTIFICATION_PLAYING_ID = 1;
    public static final int NOTIFICATION_RECORDING_ID = 2;

    public static Notification getNotification(Context context, int notificationId, RadioDetails radioDetails, CharSequence tickerText, CharSequence contentText, int flag) {
        long when = System.currentTimeMillis();
        Notification notification;

        switch (notificationId) {
            case NOTIFICATION_PLAYING_ID:
                notification = new Notification(R.drawable.ic_notification_playing, tickerText, when);
                break;
            case NOTIFICATION_RECORDING_ID:
                notification = new Notification(R.drawable.ic_notification_recording, tickerText, when);
                break;
            default:
                notification = new Notification();
        }

        CharSequence contentTitle = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, RadioActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notification.flags = flag;

        return notification;
    }

    public static void showNotification(Context context, int notificationId, RadioDetails radioDetails, CharSequence tickerText, CharSequence contentText) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        long when = System.currentTimeMillis();
        Notification notification = null;

        switch (notificationId) {
            case NOTIFICATION_PLAYING_ID:
                notification = new Notification(R.drawable.ic_notification_playing, tickerText, when);
                break;
            case NOTIFICATION_RECORDING_ID:
                notification = new Notification(R.drawable.ic_notification_recording, tickerText, when);
                break;
        }

        CharSequence contentTitle = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, RadioActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        if (notification != null) {
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            notification.flags = Notification.FLAG_ONGOING_EVENT;
        }

        notificationManager.notify(notificationId, notification);
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}
