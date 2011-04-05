package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.media.AsyncPlayer;
import android.net.Uri;
import android.os.IBinder;

import static android.media.AudioManager.STREAM_MUSIC;

public class RadioService extends Service {
    AsyncPlayer mediaPlayer;

    public void onCreate() {
        mediaPlayer = new AsyncPlayer("");
    }

    public void onDestroy() {
        mediaPlayer.stop();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.play(this, Uri.parse("http://podcast.dgen.net:8000/rinseradio"), false, STREAM_MUSIC);
        return START_STICKY;
    }
}
