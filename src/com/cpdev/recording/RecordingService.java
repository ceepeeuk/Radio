package com.cpdev.recording;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.cpdev.NotificationService;
import com.cpdev.R;
import com.cpdev.RadioApplication;
import com.cpdev.RadioDetails;

public class RecordingService extends NotificationService {

    public static final int StartRecording = 1;
    public static final int StopRecording = 2;

    private static final String TAG = "com.cpdev.recording.RecorderService";

    public RecordingService() {
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

                radioApplication.getRecordingTask().execute(radioDetails);
                Log.d(TAG, ticketText.toString());
                showNotification(NOTIFICATION_RECORDING_ID, radioDetails, ticketText, ticketText);
                break;

            case StopRecording:

                Log.d(TAG, "Stopping recording");
                RecordingTask recordingTask = radioApplication.getRecordingTask();
                recordingTask.cancel(true);

                cancelNotification(NOTIFICATION_RECORDING_ID);
                recordingTask = null;
                radioApplication.setRecordingTask(recordingTask);
                break;
        }
    }
}