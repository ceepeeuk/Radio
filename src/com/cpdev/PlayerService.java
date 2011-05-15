package com.cpdev;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import com.cpdev.utils.StringUtils;

import java.io.IOException;

public class PlayerService extends NotificationService {

    private static final String TAG = "com.cpdev.PlayerService";

    public static final int StartPlaying = 1;
    public static final int StopPlaying = 2;


    public PlayerService() {
        super("PlayerService");
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();
    }

    @Override
    protected void doWakefulWork(Intent intent) {

        Bundle bundle = intent.getExtras();

        switch (intent.getIntExtra(getString(R.string.player_service_operation_key), 1)) {

            case StartPlaying:
                RadioDetails radioDetails = new RadioDetails(
                        bundle.getString(getString(R.string.player_service_name_key)),
                        bundle.getString(getString(R.string.player_service_url_key)),
                        null);
                startPlaying(radioDetails);
                break;

            case StopPlaying:
                stopPlaying();
                break;
        }

    }

    private void stopPlaying() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }

        cancelNotification(NotificationService.NOTIFICATION_PLAYING_ID);
    }

    private void startPlaying(final RadioDetails radioDetails) {

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
                    return true;
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "OnPrepared called, so about to call MediaPlayer.start");
                    mediaPlayer.start();
                    Log.d(TAG, "Called MediaPlayer.start");

                    StringBuilder status = new StringBuilder("Playing ");
                    if (!StringUtils.IsNullOrEmpty(radioDetails.getStationName())) {
                        status.append(radioDetails.getStationName());
                    }

                    String operation = "Playing ";
                    CharSequence tickerText = StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? operation : operation + radioDetails.getStationName();
                    CharSequence contentText = StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? operation : operation + radioDetails.getStationName();
                    showNotification(NotificationService.NOTIFICATION_PLAYING_ID, radioDetails, tickerText, contentText);
                }
            });

            mediaPlayer.setDataSource(radioDetails.getStreamUrl());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();

        } catch (IOException ioe) {
            Log.e(TAG, "Error caught in play", ioe);
            mediaPlayer.reset();
        } finally {
            radioApplication.setMediaPlayer(mediaPlayer);
            radioApplication.setPlayingStation(radioDetails);
            Log.d(TAG, "Finished finally");
        }
    }


}


