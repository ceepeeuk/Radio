package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class RecorderService extends Service {

    private static final String TAG = "com.cpdev.RecorderService";
    private RadioActivity caller;

    private final IBinder mBinder = new RecorderServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void startRecording(RadioActivity view, String streamUri) {
        caller = view;
        caller.setStatus("Recording");
        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();
        radioApplication.getRecordingTask().execute(streamUri);
    }

    public void stopRecording(RadioActivity view) {
        Log.d(TAG, "Stopping recording");
        caller = view;
        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();
        RecordingTask recordingTask = radioApplication.getRecordingTask();
        recordingTask.cancel(true);

        view.setStatus("Stopped recording");

        recordingTask = null;
        radioApplication.setRecordingTask(recordingTask);
    }

    public boolean alreadyRecording() {
        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();
        RecordingTask recordingTask = radioApplication.getRecordingTask();
        return recordingTask.alreadyRecording();
    }

    public class RecorderServiceBinder extends Binder {
        RecorderService getService() {
            return RecorderService.this;
        }
    }
}
