package com.statichiss.recordio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.statichiss.R;

public abstract class NotificationHelper {

    public static final int NOTIFICATION_PLAYING_ID = 1;
    public static final int NOTIFICATION_RECORDING_ID = 2;

    public static Notification getNotification(Context context, int notificationId, RadioDetails radioDetails, CharSequence tickerText, CharSequence contentText, int flag) {
        Notification notification;

        switch (notificationId) {
            case NOTIFICATION_PLAYING_ID:
                notification = new Notification.Builder(context)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(contentText)
                        .setTicker(tickerText)
                        .setSmallIcon(R.drawable.ic_notification_playing)
//                        .setLargeIcon(aBitmap)
                        .build();
                break;
            case NOTIFICATION_RECORDING_ID:
                notification = new Notification.Builder(context)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(contentText)
                        .setTicker(tickerText)
                        .setSmallIcon(R.drawable.ic_notification_recording)
//                        .setLargeIcon(aBitmap)
                        .build();
                break;
            default:
                notification = new Notification();
        }

        notification.contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        notification.flags = flag;

        return notification;
    }

    public static void showNotification(Context context, int notificationId, CharSequence tickerText, CharSequence contentText) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = null;

        switch (notificationId) {
            case NOTIFICATION_PLAYING_ID:
                notification = new Notification.Builder(context)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(contentText)
                        .setTicker(tickerText)
                        .setSmallIcon(R.drawable.ic_notification_playing)
//                        .setLargeIcon(aBitmap)
                        .build();
                break;
            case NOTIFICATION_RECORDING_ID:
                notification = new Notification.Builder(context)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(contentText)
                        .setTicker(tickerText)
                        .setSmallIcon(R.drawable.ic_notification_recording)
//                        .setLargeIcon(aBitmap)
                        .build();
                break;
        }

        if (notification != null) {
            notification.contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(notificationId, notification);
        }
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}
