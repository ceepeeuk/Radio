package com.statichiss.recordio;

import android.app.Application;
import android.media.MediaPlayer;

public class RadioApplication extends Application {

    private RadioDetails _playingStation;
    private RadioDetails _recordingStation;
    private MediaPlayer _mediaPlayer;
    private String _playingStatus;
    private String _recordingStatus;

    public static final int StartPlayingRadio = 1;
    public static final int StartPlayingFile = 2;
    public static final int PausePlaying = 3;
    public static final int ResumePlaying = 4;
    public static final int StopPlaying = 5;

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

    public RadioDetails getRecordingStation() {
        return _recordingStation;
    }

    public void setRecordingStation(RadioDetails recordingStation) {
        _recordingStation = recordingStation;
    }

    public String getPlayingStatus() {
        return _playingStatus == null ? "" : _playingStatus;
    }

    public void setPlayingStatus(String playingStatus) {
        this._playingStatus = playingStatus;
    }

    public String getRecordingStatus() {
        return _recordingStatus == null ? "" : _recordingStatus;
    }

    public void setRecordingStatus(String recordingStatus) {
        this._recordingStatus = recordingStatus;
    }
}
