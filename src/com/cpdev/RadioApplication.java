package com.cpdev;

import android.app.Application;
import android.media.MediaPlayer;

public class RadioApplication extends Application {
    private RecordingTask recordingTask;
    private MediaPlayer mediaPlayer;

    public RecordingTask getRecordingTask() {
        if (recordingTask == null) {
            recordingTask = new RecordingTask();
        }
        return recordingTask;
    }

    public void setRecordingTask(RecordingTask recordingTask) {
        this.recordingTask = recordingTask;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }
}
