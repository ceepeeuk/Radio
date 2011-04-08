package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class RadioService extends Service implements MediaPlayer.OnBufferingUpdateListener {

    MediaPlayer mediaPlayer;
    private static final String rinseUri = "http://podcast.dgen.net:8000/rinseradio";
    private static final String TAG = "RadioService";

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer.stop();
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        new RadioServiceTask().execute(rinseUri);
        //mediaPlayer.play(this, Uri.parse("http://podcast.dgen.net:8000/rinseradio"), false, STREAM_MUSIC);
        return START_STICKY;
    }

    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.i(TAG, "Buffering = " + i + "%");
    }

    private class RadioServiceTask extends AsyncTask<String, Integer, Long> implements MediaPlayer.OnBufferingUpdateListener {

        @Override
        protected Long doInBackground(String... strings) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(rinseUri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        Log.d(TAG, "Started playing");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers) {
            Log.e(TAG, "Percentage: " + integers[0]);
        }

        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
            Log.i(TAG, "Buffering = " + i + "%");
        }
    }
}
