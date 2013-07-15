package com.statichiss.recordio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.statichiss.R;
import com.statichiss.recordio.filehandling.M3uHandler;
import com.statichiss.recordio.filehandling.PlsHandler;

import java.io.File;
import java.io.IOException;

public class PlayerService extends WakefulIntentService implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "com.statichiss.recordio.PlayerService";

    public PlayerService() {
        super("PlayerService");
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
                playStream(radioDetails);
                break;
            case RadioApplication.StartPlayingFile:
                String file = bundle.getString(getString(R.string.player_service_file_name_key));
                playFile(file);
                break;
            case RadioApplication.PausePlaying:
                pause();
                break;
            case RadioApplication.ResumePlaying:
                resume();
                break;
            case RadioApplication.StopPlaying:
                stop();
                break;
            default:
                Log.e(TAG, "Unexpected operation delivered to PlayerService");
        }
    }

    private void updateActivity(String text) {
        Intent intent = new Intent(getString(R.string.player_service_update_playing_key));
        ((RadioApplication) getApplication()).setPlayingStatus(text);
        getApplicationContext().sendBroadcast(intent);
    }

    private void sendError(String radioDetails, String exceptionMessage) {
        Intent intent = new Intent(getString(R.string.player_service_update_playing_error_key));
        intent.putExtra(getString(R.string.player_service_update_playing_error_radio_details), radioDetails);
        intent.putExtra(getString(R.string.player_service_update_playing_error_exception), exceptionMessage);
        getApplicationContext().sendBroadcast(intent);
    }

    private void playStream(RadioDetails radioDetails) {
        final RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        final RadioDetails incomingRadioDetails = radioDetails;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
            // No network connection
            String error = "Failed to play " + incomingRadioDetails.getStationName() + ", network unavailable";
            Notification errorNotification = NotificationHelper.getNotification(this, NotificationHelper.NOTIFICATION_PLAYING_ID, error, error, Notification.FLAG_ONLY_ALERT_ONCE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NotificationHelper.NOTIFICATION_PLAYING_ID, errorNotification);
            updateActivity("Network Unavailable");
            Log.e(TAG, error);
            return;
        }

        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        try {

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                radioApplication.setMediaPlayer(mediaPlayer);
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "onCompletion called");
                    if (mediaPlayer.isPlaying()) {    //should be false if error occurred
                        mediaPlayer.start();
                    }
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    Log.e(TAG, "Error occurred trying to play: " + incomingRadioDetails);
                    updateActivity("Error playing");
                    sendError(incomingRadioDetails.toString(), "onError invoked");
                    radioApplication.setBuffering(false);
                    mediaPlayer.reset();
                    return false;
                }
            });

            if (incomingRadioDetails.getPlaylistUrl().endsWith(".pls") || incomingRadioDetails.getPlaylistUrl().endsWith(".m3u")) {
                String basePath = getApplicationContext().getFilesDir().getPath();
                if (incomingRadioDetails.getPlaylistUrl().endsWith(".pls")) {
                    radioDetails = PlsHandler.parse(radioDetails, basePath);
                } else {
                    radioDetails = M3uHandler.parse(radioDetails, basePath);
                }
            } else {
                radioDetails.setStreamUrl(radioDetails.getPlaylistUrl());
            }

            updateActivity(getString(R.string.buffering_string) + " " + radioDetails.getStationName());
            radioApplication.setPlayingStatus(getString(R.string.buffering_string) + " " + radioDetails.getStationName());

            // need to check we aren't buffering before proceeding
            if (radioApplication.isBuffering()) {
                Log.d(TAG, "Buffering already, so resetting MediaPlayer before starting again");
                while (radioApplication.isBuffering()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(radioDetails.getStreamUrl());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepareAsync();
            radioApplication.setBuffering(true);

            final RadioDetails radioDetailsToPlay = radioDetails;

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (getFocus()) {
                        mediaPlayer.start();
                        radioApplication.setBuffering(false);
                        radioApplication.setPlayingType(RadioApplication.PlayingStream);
                        String status = getString(R.string.playing_string) + " " + radioDetailsToPlay.getStationName();
                        updateActivity(status);
                        NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.NOTIFICATION_PLAYING_ID, status, status);
                    }
                }
            });

        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "IllegalArgumentException caught in PlayService for url: " + radioDetails.getStreamUrl(), iae);
            updateActivity("Error trying to play stream");
            mediaPlayer.reset();
            sendError(radioDetails.toString(), iae.toString());
        } catch (IOException ioe) {
            Log.e(TAG, "IOException caught in PlayService for url: " + radioDetails.getStreamUrl(), ioe);
            updateActivity("Error trying to play stream");
            mediaPlayer.reset();
            sendError(radioDetails.toString(), ioe.getMessage());
        } finally {
            //radioApplication.setMediaPlayer(mediaPlayer);
            radioApplication.setPlayingStation(radioDetails);
        }
    }

    private boolean getFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void playFile(final String file) {

        final RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        try {

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                radioApplication.setMediaPlayer(mediaPlayer);
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    NotificationHelper.cancelNotification(getApplicationContext(), NotificationHelper.NOTIFICATION_PLAYING_ID);
                    updateActivity("");
                    mediaPlayer.reset();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    Log.e(TAG, "Error occurred trying to play: " + file);
                    updateActivity("Error playing " + file);
                    return false;
                }
            });

            String recFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name);

            // need to check we aren't buffering before proceeding
            if (radioApplication.isBuffering()) {
                Log.d(TAG, "Buffering already, so resetting MediaPlayer before starting again");
                updateActivity("Preparing to play " + file);
                while (radioApplication.isBuffering()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                mediaPlayer.reset();
            }

            // TODO - cater for already playing stream

            mediaPlayer.setDataSource(recFolder + File.separator + file);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (getFocus()) {
                        radioApplication.setPlayingType(RadioApplication.PlayingFile);
                        radioApplication.setPlayingFileDetails(new PlayingFile(mediaPlayer.getDuration(), file));

                        // If filename is in session, then retrieve position and jump to it
                        if (radioApplication.getLastPlayedFile() != null && file.equals(radioApplication.getLastPlayedFile().getName())) {
                            if (mediaPlayer.getDuration() > radioApplication.getLastPlayedFile().getCurrentPosition()) {
                                mediaPlayer.seekTo(radioApplication.getLastPlayedFile().getCurrentPosition());
                            }
                        }

                        mediaPlayer.start();
                        String status = getString(R.string.playing_string) + " " + file;
                        updateActivity(status);
                        NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.NOTIFICATION_PLAYING_ID, status, status);
                    }
                }
            });

        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "IllegalArgumentException caught in PlayService for file: " + file, iae);
            updateActivity("Error trying to play stream");
            mediaPlayer.reset();
        } catch (IOException ioe) {
            Log.e(TAG, "IOException caught in PlayService for file: " + file, ioe);
            updateActivity("Error trying to play stream");
            mediaPlayer.reset();
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
        updateActivity("Paused");
    }

    private void resume() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
        updateActivity("Resumed playing " + radioApplication.getPlayingStation().getStationName());
    }

    private void stop() {
        RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying() && radioApplication.getPlayingFileDetails() != null) {
                radioApplication.setLastPlayedFile(new LastPlayedFile(radioApplication.getPlayingFileDetails().getName(), mediaPlayer.getCurrentPosition()));
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }

        radioApplication.setMediaPlayer(null);
        updateActivity("");
        NotificationHelper.cancelNotification(getApplicationContext(), NotificationHelper.NOTIFICATION_PLAYING_ID);
    }


    @Override
    public void onAudioFocusChange(int focusChange) {

        final RadioApplication radioApplication = (RadioApplication) getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    radioApplication.setMediaPlayer(mediaPlayer);
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
//                if (mediaPlayer.isPlaying())
//                    mediaPlayer.stop();
////                mediaPlayer.release();
//                radioApplication.setMediaPlayer(null);
                stop();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }
}
