package com.cpdev.recording;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.cpdev.R;
import com.cpdev.RadioApplication;
import com.cpdev.RadioDetails;

public class RecordingService extends WakefulIntentService {

    public final int StartRecording = 1;
    public final int StopRecording = 2;

    private static final String TAG = "com.cpdev.recording.RecorderService";
    private static final int notificationId = 0;

    public RecordingService(String name) {
        super("RecordingService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {

        Bundle bundle = intent.getExtras();
        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();

        switch (intent.getIntExtra(getString(R.string.timed_recorder_service_operation_key), 1)) {

            case StartRecording:
                RadioDetails radioDetails = new RadioDetails(
                        bundle.getString(getString(R.string.timed_recorder_service_name_key)),
                        bundle.getString(getString(R.string.timed_recorder_service_url_key)),
                        null);

                CharSequence ticketText = new StringBuilder()
                        .append("Recording ")
                        .append(radioDetails.getStationName())
                        .toString();

                startForeground(notificationId, new Notification(R.drawable.ic_notification_recording, ticketText, System.currentTimeMillis()));

                radioApplication.getRecordingTask().execute(radioDetails);
                break;

            case StopRecording:

                Log.d(TAG, "Stopping recording");
                RecordingTask recordingTask = radioApplication.getRecordingTask();
                recordingTask.cancel(true);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);

                recordingTask = null;
                radioApplication.setRecordingTask(recordingTask);
                break;
        }
    }
}
