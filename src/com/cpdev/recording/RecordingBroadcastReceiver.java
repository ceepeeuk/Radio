package com.cpdev.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.cpdev.R;

public class RecordingBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "com.cpdev.recording.RecordingBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //TimedRecorderService.sendWakefulWork(context, intent);
        Bundle bundle = intent.getExtras();
        Log.e(TAG, "StationName = " + bundle.getString(context.getString(R.string.timed_recorder_service_name_key)));
        Log.e(TAG, "StationURL = " + bundle.getString(context.getString(R.string.timed_recorder_service_url_key)));
        Log.e(TAG, "Duration = " + bundle.getLong(context.getString(R.string.timed_recorder_service_recording_duration)));
        Log.e(TAG, "Type = " + bundle.getLong(context.getString(R.string.timed_recorder_service_operation_key)));

        // Need to fire off service request here and then if one-off delete from table.


    }
}
