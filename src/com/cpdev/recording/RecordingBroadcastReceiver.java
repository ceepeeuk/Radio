package com.cpdev.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.cpdev.R;
import com.cpdev.RadioApplication;

public class RecordingBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "com.cpdev.recording.RecordingBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent receivedIntent) {

        Bundle bundle = receivedIntent.getExtras();

        // Need to fire off service request here and then if one-off delete from table.
        Intent newIntent = new Intent("com.cpdev.recording.RecorderService");
        newIntent.putExtra(context.getString(R.string.timed_recorder_service_name_key),
                bundle.getString(context.getString(R.string.timed_recorder_service_name_key)));
        newIntent.putExtra(context.getString(R.string.timed_recorder_service_url_key),
                bundle.getString(context.getString(R.string.timed_recorder_service_url_key)));
        newIntent.putExtra(context.getString(R.string.timed_recorder_service_recording_duration),
                bundle.getLong(context.getString(R.string.timed_recorder_service_recording_duration)));
        newIntent.putExtra(context.getString(R.string.timed_recorder_service_operation_key),
                bundle.getLong(context.getString(R.string.timed_recorder_service_operation_key)));

        RecorderService.sendWakefulWork(context, newIntent);

        long type = bundle.getLong(context.getString(R.string.timed_recorder_service_operation_key));

        if (type == RadioApplication.ONE_OFF_SCHEDULED_RECORDING) {
            // delete from db
        }

    }
}
