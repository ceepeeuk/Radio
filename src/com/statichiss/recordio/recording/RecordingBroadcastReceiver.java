package com.statichiss.recordio.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.statichiss.R;
import com.statichiss.recordio.AlarmHelper;
import com.statichiss.recordio.DatabaseHelper;
import com.statichiss.recordio.RadioDetails;

import java.io.IOException;

public class RecordingBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "com.statichiss.recordio.recording.RecordingBroadcastReceiver";

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
        Intent newIntent = new Intent("com.statichiss.recordio.recording.RecorderService");
        newIntent.putExtra(context.getString(R.string.radio_details_key), radioDetails);

        RecorderService.sendWakefulWork(context, newIntent);

        if (databaseId > 0) {

            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            try {
                databaseHelper.openDataBase();

                switch ((int) radioDetails.getRecordingType()) {
                    case AlarmHelper.ONE_OFF_SCHEDULED_RECORDING:
                        databaseHelper.deleteScheduledRecording(databaseId);
                        break;
                    case AlarmHelper.DAILY_SCHEDULED_RECORDING:
                        databaseHelper.updateScheduledRecordingAddDay(databaseId);
                        break;
                    case AlarmHelper.WEEKLY_SCHEDULED_RECORDING:
                        databaseHelper.updateScheduledRecordingAddWeek(databaseId);
                        break;
                    default:
                        Log.e(TAG, "Unexpected recordingType received - " + radioDetails.getRecordingType());
                }

            } catch (IOException e) {
                Log.e(TAG, "IOException thrown when trying to access DB", e);
            } finally {
                databaseHelper.close();
            }

        }
    }

}
