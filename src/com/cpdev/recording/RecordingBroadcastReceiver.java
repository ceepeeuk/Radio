package com.cpdev.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.cpdev.R;
import com.cpdev.RadioDetails;

public class RecordingBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "com.cpdev.recording.RecordingBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent receivedIntent) {

        Bundle bundle = receivedIntent.getExtras();
        RadioDetails radioDetails = bundle.getParcelable(context.getString(R.string.radio_details_key));


        // Need to fire off service request here and then if one-off delete from table.
        Intent newIntent = new Intent("com.cpdev.recording.RecorderService");
        newIntent.putExtra(context.getString(R.string.radio_details_key), radioDetails);

//        intent.putExtra(context.getString(R.string.timed_recorder_service_recording_duration), endDateTime - startDateTime);
//                intent.putExtra(context.getString(R.string.timed_recorder_service_operation_key), typeId);
//
//        newIntent.putExtra(context.getString(R.string.timed_recorder_service_name_key),
//                bundle.getString(context.getString(R.string.timed_recorder_service_name_key)));
//
//        newIntent.putExtra(context.getString(R.string.timed_recorder_service_url_key),
//                bundle.getString(context.getString(R.string.timed_recorder_service_url_key)));
//

//        newIntent.putExtra(context.getString(R.string.timed_recorder_service_operation_key),
//                bundle.getLong(context.getString(R.string.timed_recorder_service_operation_key)));
//
//        long type = bundle.getLong(context.getString(R.string.timed_recorder_service_operation_key));
//
//        newIntent.putExtra(context.getString(R.string.timed_recorder_service_recording_duration), type);

        RecorderService.sendWakefulWork(context, newIntent);


        if (radioDetails.getRecordingType() == RadioDetails.ONE_OFF_SCHEDULED_RECORDING) {
            // delete from db
        }

    }
}
