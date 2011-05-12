package com.cpdev.recording;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.cpdev.NotificationService;
import com.cpdev.RadioActivity;
import com.cpdev.RadioApplication;
import com.cpdev.RadioDetails;
import com.cpdev.utils.StringUtils;

public class RecorderService extends NotificationService {

    private static final String TAG = "com.cpdev.recording.RecorderService";
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

    public void startRecording(RadioActivity view, RadioDetails radioDetails) {
        caller = view;
        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();
        radioApplication.getRecordingTask().execute(radioDetails);
        radioApplication.getRecordingTask().attach(view);

        String operation = StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? "Recording " : "Recording " + radioDetails.getStationName();
        caller.setStatus(operation);
//        CharSequence tickerText = StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? operation : operation + radioDetails.getStationName();
//        CharSequence contentText = StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? operation : operation + radioDetails.getStationName();
        showNotification(NotificationService.RECORDING_ID, radioDetails, operation, operation);
    }

    public void stopRecording(RadioActivity view) {
        Log.d(TAG, "Stopping recording");
        caller = view;
        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();
        RecordingTask recordingTask = radioApplication.getRecordingTask();
        recordingTask.cancel(true);

        view.setStatus("Stopped recording");
        cancelNotification(NotificationService.RECORDING_ID);

        recordingTask = null;
        radioApplication.setRecordingTask(recordingTask);
    }

    public boolean alreadyRecording() {
        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();
        RecordingTask recordingTask = radioApplication.getRecordingTask();
        return recordingTask.alreadyRecording();
    }

    public class RecorderServiceBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }
    }
}
