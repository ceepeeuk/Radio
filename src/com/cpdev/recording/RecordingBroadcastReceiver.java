package com.cpdev.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.cpdev.AlarmHelper;
import com.cpdev.DatabaseHelper;
import com.cpdev.R;
import com.cpdev.RadioDetails;

import java.io.IOException;

public class RecordingBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "com.cpdev.recording.RecordingBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent receivedIntent) {

        Bundle bundle = receivedIntent.getExtras();

        // Whilst the below line should work, it doesn't, so have to manually marshall and unmarshall.
        //RadioDetails radioDetails = bundle.getParcelable(context.getString(R.string.radio_details_key));

        long databaseId = bundle.getLong(context.getString(R.string.timed_recorder_service_database_id_key));

        RadioDetails radioDetails = new RadioDetails(
                bundle.getString(context.getString(R.string.radio_details_name_key)),
                bundle.getString(context.getString(R.string.radio_details_stream_url_key)),
                bundle.getString(context.getString(R.string.radio_details_playlist_url_key)),
                bundle.getLong(context.getString(R.string.timed_recorder_service_recording_duration)),
                bundle.getLong(context.getString(R.string.timed_recorder_service_operation_key))
        );

        // Need to fire off service request here and then if one-off delete from table.
        Intent newIntent = new Intent("com.cpdev.recording.RecorderService");
        newIntent.putExtra(context.getString(R.string.radio_details_key), radioDetails);

        RecorderService.sendWakefulWork(context, newIntent);

        if (radioDetails.getRecordingType() == AlarmHelper.ONE_OFF_SCHEDULED_RECORDING && databaseId > 0) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            try {
                databaseHelper.createDataBase();
                databaseHelper.openDataBase();
                databaseHelper.deleteScheduledRecording(databaseId);
            } catch (IOException e) {
                Log.e(TAG, "IOException thrown when trying to access DB", e);
            } finally {
                databaseHelper.close();
            }
        }
    }

}
