package com.cpdev;

import android.app.Application;
import android.media.MediaPlayer;

public class RadioApplication extends Application {

    // Table not zero indexed, so starts at 1
    public static final int ONE_OFF_SCHEDULED_RECORDING = 1;
    public static final int DAILY_SCHEDULED_RECORDING = 2;
    public static final int WEEKLY_SCHEDULED_RECORDING = 3;

    private RadioDetails _playingStation;
    private MediaPlayer _mediaPlayer;

    public MediaPlayer getMediaPlayer() {
        return _mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this._mediaPlayer = mediaPlayer;
    }

    public RadioDetails getPlayingStation() {
        return _playingStation;
    }

    public void setPlayingStation(RadioDetails playingStation) {
        _playingStation = playingStation;
    }
}
