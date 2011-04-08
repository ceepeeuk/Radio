package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class RadioService extends Service implements MediaPlayer.OnBufferingUpdateListener {
    MediaPlayer mediaPlayer;
    String rinseUri = "http://podcast.dgen.net:8000/rinseradio";
    private static final String TAG = "RadioService";

    public void onCreate() {
    }

    public void onDestroy() {
        mediaPlayer.release();
        mediaPlayer.stop();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(rinseUri);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //mediaPlayer.play(this, Uri.parse("http://podcast.dgen.net:8000/rinseradio"), false, STREAM_MUSIC);
        return START_STICKY;
    }

    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.i(TAG, "Buffering = " + i + "%");
    }
}
