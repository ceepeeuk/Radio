package com.statichiss.recordio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.statichiss.R;
import com.statichiss.recordio.filehandling.M3uHandler;
import com.statichiss.recordio.filehandling.PlsHandler;
import com.statichiss.recordio.utils.StringUtils;

import java.io.IOException;

public class WakefulPlayerService extends WakefulIntentService {

    private static final String TAG = "com.statichiss.recordio.WakefulPlayerService";
    private boolean buffering = false;

    public WakefulPlayerService() {
        super("WakefulPlayerService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {

        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
        super.onCreate();

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        radioApplication.setMediaPlayer(mediaPlayer);

        Bundle bundle = intent.getExtras();
        int operation = bundle.getInt(getString(R.string.player_service_operation_key));

        switch (operation) {
            case RadioApplication.StartPlayingRadio:
                RadioDetails radioDetails = bundle.getParcelable(getString(R.string.radio_details_key));
                play(radioDetails);
                break;
            case RadioApplication.PausePlayingRadio:
                pause();
                break;
            case RadioApplication.ResumePlayingRadio:
                resume();
                break;
            case RadioApplication.StopPlayingRadio:
                stop();
                break;
            default:
                Log.e(TAG, "Unexpected operation delivered to WakefulPlayerService");
        }
    }

    private void updateActivity(boolean status, String text) {
        Intent intent = new Intent(getString(R.string.player_service_update_playing_key));
        intent.putExtra(getString(R.string.player_service_update_playing_status), status);
        intent.putExtra(getString(R.string.player_service_update_playing_text), text);
        getApplicationContext().sendBroadcast(intent);
    }

    private void sendError(String radioDetails, String exceptionMessage) {
        Intent intent = new Intent(getString(R.string.player_service_update_playing_error_key));
        intent.putExtra(getString(R.string.player_service_update_playing_error_radio_details), radioDetails);
        intent.putExtra(getString(R.string.player_service_update_playing_error_exception), exceptionMessage);
        getApplicationContext().sendBroadcast(intent);
    }

    private void play(RadioDetails radioDetails) {
        final RadioDetails incomingRadioDetails = radioDetails;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
            // No network connection
            String error = "Failed to play " + incomingRadioDetails.getStationName() + ", network unavailable";
            Notification errorNotification = NotificationHelper.getNotification(this, NotificationHelper.NOTIFICATION_PLAYING_ID, incomingRadioDetails, error, error, Notification.FLAG_ONLY_ALERT_ONCE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NotificationHelper.NOTIFICATION_PLAYING_ID, errorNotification);
            updateActivity(false, "Network Unavailable");
            Log.e(TAG, error);
            return;
        }

        updateActivity(true, getString(R.string.buffering_string) + " " + incomingRadioDetails.getStationName());

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
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    Log.e(TAG, "Error occurred trying to play: " + incomingRadioDetails);
                    updateActivity(true, "Error playing");
                    sendError(incomingRadioDetails.toString(), "onError invoked");
                    return false;
                }
            });

            if (incomingRadioDetails.getPlaylistUrl().endsWith(".pls") || incomingRadioDetails.getPlaylistUrl().endsWith(".m3u")) {
                if (incomingRadioDetails.getPlaylistUrl().endsWith(".pls")) {
                    radioDetails = PlsHandler.parse(radioDetails);
                } else {
                    radioDetails = M3uHandler.parse(radioDetails);
                }
            } else {
                radioDetails.setStreamUrl(radioDetails.getPlaylistUrl());
            }

            // need to check we aren't buffering before proceeding
            if (buffering) {
                Log.d(TAG, "Buffering already, so resetting MediaPlayer before starting again");
                mediaPlayer.reset();
                buffering = false;
            }

            mediaPlayer.setDataSource(radioDetails.getStreamUrl());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepareAsync();
            buffering = true;

            final RadioDetails radioDetailsToPlay = radioDetails;

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    buffering = false;
                    StringBuilder status = new StringBuilder(getString(R.string.playing_string));
                    if (!StringUtils.IsNullOrEmpty(radioDetailsToPlay.getStationName())) {
                        status.append(" ")
                                .append(radioDetailsToPlay.getStationName());
                    }
                    updateActivity(true, status.toString());
                    NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.NOTIFICATION_PLAYING_ID, radioDetailsToPlay, status.toString(), status.toString());
                }
            });

        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "IllegalArgumentException caught in PlayService for url: " + radioDetails.getStreamUrl(), iae);
            updateActivity(false, "Error trying to play stream");
            mediaPlayer.reset();
            sendError(radioDetails.toString(), iae.toString());
        } catch (IOException ioe) {
            Log.e(TAG, "IOException caught in PlayService for url: " + radioDetails.getStreamUrl(), ioe);
            updateActivity(false, "Error trying to play stream");
            mediaPlayer.reset();
            sendError(radioDetails.toString(), ioe.getMessage());
        } finally {
            radioApplication.setMediaPlayer(mediaPlayer);
            radioApplication.setPlayingStation(radioDetails);
        }
    }

    private void pause() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
        updateActivity(false, "Paused");
    }

    private void resume() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
        updateActivity(false, "Resumed");
    }

    private void stop() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }

        radioApplication.setMediaPlayer(null);
        updateActivity(false, "");
        NotificationHelper.cancelNotification(getApplicationContext(), NotificationHelper.NOTIFICATION_PLAYING_ID);
    }


}
