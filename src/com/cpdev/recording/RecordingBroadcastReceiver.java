package com.cpdev.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RecordingBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "com.cpdev.recording.RecordingBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //TimedRecorderService.sendWakefulWork(context, intent);
        Log.e(TAG, "In onReceive");
        Bundle bundle = intent.getExtras();
        Log.e(TAG, "StationName = " + bundle.getString("StationName"));
        Log.e(TAG, "StationURL = " + bundle.getString("StationURL"));
        Log.e(TAG, "duration = " + bundle.getString("duration"));
    }
}
