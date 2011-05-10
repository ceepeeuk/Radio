package com.cpdev;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.cpdev.utils.StringUtils;

import java.io.IOException;

public class PlayerService extends Service {

    private static final String TAG = "com.cpdev.PlayerService";
    RadioActivity caller;

    private final IBinder mBinder = new RadioServiceBinder();
    private static final int PLAYING_ID = 1;


    public boolean alreadyPlaying() {
        MediaPlayer mediaPlayer = ((RadioApplication) getApplicationContext()).getMediaPlayer();
        if (mediaPlayer != null) {
            boolean playing = mediaPlayer.isPlaying();
            return playing;
        } else {
            return false;
        }
    }

    @Override
    public void onCreate() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
        super.onCreate();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        radioApplication.setMediaPlayer(mediaPlayer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void stopPlaying() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PLAYING_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startPlaying(RadioActivity view, final RadioDetails radioDetails) {

        caller = view;
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        try {

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (mediaPlayer.isPlaying()) {    //should be false if error occurred
                        mediaPlayer.start();
                    }
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.e(TAG, "Damn error occurred");
                    caller.setStatus("Error");
                    return true;
                }
            });

            mediaPlayer.setDataSource(radioDetails.getStreamUrl());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    StringBuilder status = new StringBuilder("Playing ");
                    if (!StringUtils.IsNullOrEmpty(radioDetails.getStationName())) {
                        status.append(radioDetails.getStationName());
                    }
                    caller.setStatus(status.toString());
                    showNotification(radioDetails);
                }
            });


        } catch (IOException ioe) {
            Log.e(TAG, "Error caught in play", ioe);
            ioe.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            mediaPlayer.reset();
        } finally {
            radioApplication.setMediaPlayer(mediaPlayer);
            radioApplication.setPlayingStation(radioDetails);
        }
    }

    private void showNotification(RadioDetails radioDetails) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int icon = R.drawable.ic_notification_playing;
        CharSequence tickerText = StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? "Playing" : "Playing " + radioDetails.getStationName();
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);

        Context context = getApplicationContext();
        CharSequence contentTitle = getString(R.string.app_name);
        CharSequence contentText = StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? "Playing" : "Playing " + radioDetails.getStationName();
        Intent notificationIntent = new Intent(this, RadioActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notificationManager.notify(PLAYING_ID, notification);
    }

    public class RadioServiceBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }
}


