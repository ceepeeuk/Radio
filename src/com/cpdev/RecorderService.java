package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class RecorderService extends Service {

    private static final String TAG = "RecorderService";
    private RadioActivity caller;
    private RecordingTask recordingTask;

    private final IBinder mBinder = new RecorderServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        if (recordingTask == null) {
            recordingTask = RecordingTask.getInstance();
        }
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void startRecording(RadioActivity view, String streamUri) {
        caller = view;
        caller.setStatus("Recording");
        recordingTask.execute(streamUri);
    }

    public void stopRecording(RadioActivity view) {
        Log.d(TAG, "Stopping recording");
        caller = view;
        if (recordingTask != null) {
            recordingTask.cancel(true);
        }
        view.setStatus("Stopped recording");
    }

    public boolean alreadyRecording() {
        return recordingTask.alreadyRecording();
    }

    public class RecorderServiceBinder extends Binder {
        RecorderService getService() {
            return RecorderService.this;
        }
    }
}
