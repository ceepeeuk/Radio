package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class RadioService extends Service implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {
    MediaPlayer mediaPlayer;
    Uri rinseUri = Uri.parse("http://podcast.dgen.net:8000/rinseradio");
    String TAG = "RadioService";

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mediaPlayer.setDataSource(this, rinseUri);
            mediaPlayer.prepare();
            mediaPlayer.setOnBufferingUpdateListener(this);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return START_STICKY;
    }

    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        Log.d(TAG, "Buffered: " + percent + "%");
    }

    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.e(TAG, "onError--->   what:" + what + "    extra:" + extra);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        return false;
    }

}
