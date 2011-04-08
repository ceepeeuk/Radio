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
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
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

    private class RadioServiceTask extends AsyncTask<String, Integer, Long> implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {

        @Override
        protected Long doInBackground(String... strings) {
            try {
                mediaPlayer = new MediaPlayer();

                mediaPlayer.setDataSource(rinseUri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnBufferingUpdateListener(this);
                mediaPlayer.setOnErrorListener(this);
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

        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
            Log.i(TAG, "Buffering = " + i + "%");
            Log.i(TAG, "GetCurrentPosition = " + mediaPlayer.getCurrentPosition() / 100);
        }

        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
