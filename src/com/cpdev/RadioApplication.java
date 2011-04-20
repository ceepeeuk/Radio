package com.cpdev;

import android.app.Application;

public class RadioApplication extends Application {
    private RecordingTask recordingTask;
    private SingletonMediaPlayer singletonMediaPlayer;

    public RecordingTask getRecordingTask() {
        if (recordingTask == null) {
            recordingTask = new RecordingTask();
        }
        return recordingTask;
    }

    public void setRecordingTask(RecordingTask recordingTask) {
        this.recordingTask = recordingTask;
    }

    public SingletonMediaPlayer getSingletonMediaPlayer() {
        return singletonMediaPlayer;
    }

    public void setSingletonMediaPlayer(SingletonMediaPlayer singletonMediaPlayer) {
        this.singletonMediaPlayer = singletonMediaPlayer;
    }
}
