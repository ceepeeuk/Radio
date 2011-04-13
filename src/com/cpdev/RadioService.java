package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class RadioService extends Service {

    private static final String folder = "/sdcard/Radio/";
    private static final String TAG = "RadioService";
    MediaPlayer mediaPlayer;
    RadioActivity caller;

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

    public void start(RadioActivity view, String streamUri) {
        caller = view;
        try {

            if (mediaPlayer == null) {
                mediaPlayer = SingletonMediaPlayer.getInstance();
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "On completion called");
                    if (mediaPlayer.isPlaying()) {    //should be false if error occurred
                        mediaPlayer.start();
                        caller.updateUI(true);
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

            mediaPlayer.setDataSource(streamUri);
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

    public void record(RadioActivity view, String streamUri) {
        caller = view;
        URL url = null;

        try {
            url = new URL(streamUri);
            InputStream inputStream = url.openStream();
            Log.d(TAG, "url.openStream()");

            String recFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Radio";

            if (!new File(recFolder).exists()) {
                new File(recFolder).mkdir();
            }

            String outputSource = recFolder + File.separator + "test.mp3";
            FileOutputStream fileOutputStream = new FileOutputStream(outputSource);
            Log.d(TAG, "FileOutputStream: " + fileOutputStream.toString());

            int c;
            int bytesRead = 0;

            while ((c = inputStream.read()) != -1) {
                Log.d(TAG, "bytesRead=" + bytesRead);
                fileOutputStream.write(c);
                bytesRead++;
            }

            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (MalformedURLException e) {
            Log.e(TAG, "Uri malformed: " + streamUri, e);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage(), e);
        }
    }

    public class RadioServiceBinder extends Binder {
        RadioService getService() {
            return RadioService.this;
        }
    }
}


