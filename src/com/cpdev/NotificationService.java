package com.cpdev;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public abstract class NotificationService extends WakefulIntentService {

    protected static final int NOTIFICATION_PLAYING_ID = 1;
    protected static final int NOTIFICATION_RECORDING_ID = 2;

    public NotificationService(String name) {
        super("NotificationService");
    }


    protected void showNotification(int notificationId, RadioDetails radioDetails, CharSequence tickerText, CharSequence contentText) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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

        Context context = getApplicationContext();
        CharSequence contentTitle = getString(R.string.app_name);
        Intent notificationIntent = new Intent(this, RadioActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(notificationId, notification);
    }

    protected void cancelNotification(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

}
