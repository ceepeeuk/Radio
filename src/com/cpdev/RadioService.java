package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class RadioService extends Service {

    MediaPlayer mediaPlayer;
    RadioActivity caller;
    private static final String rinseUri = "http://podcast.dgen.net:8000/rinseradio";
    private static final String TAG = "RadioService";

    private final IBinder mBinder = new RadioServiceBinder();

    public boolean alreadyPlaying() {
        if (mediaPlayer != null) {
            Log.d(TAG, "mediaPlayer.getAudioSessionId: " + mediaPlayer.getAudioSessionId());
            return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mediaPlayer == null) {
            mediaPlayer = SingletonMediaPlayer.getInstance();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void start(RadioActivity radioActivity) {
        caller = radioActivity;
        try {

            if (mediaPlayer == null) {
                mediaPlayer = SingletonMediaPlayer.getInstance();
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "On completion called");
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

            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                    Log.i(TAG, "onInfo called with: " + what);
                    return true;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });

            mediaPlayer.setDataSource(rinseUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    caller.setStatus("Playing");
                }
            });


        } catch (IOException ioe) {
            Log.e(TAG, "Error caught in play", ioe);
            ioe.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            mediaPlayer.reset();
        }
    }

    public class RadioServiceBinder extends Binder {
        RadioService getService() {
            return RadioService.this;
        }
    }
}


