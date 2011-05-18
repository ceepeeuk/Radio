package com.cpdev;

import android.app.Application;
import android.media.MediaPlayer;

public class RadioApplication extends Application {
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
