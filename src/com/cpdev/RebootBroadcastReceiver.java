package com.cpdev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.io.IOException;

public class RebootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "com.cpdev.RebootBroadcastReceiver";

    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive called");

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        AlarmHelper alarmHelper = new AlarmHelper();

        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        Cursor cursor = dbHelper.getAllScheduledRecordings();

        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            alarmHelper.setAlarm(context, cursor.getLong(0), cursor.getLong(3), cursor.getLong(4), cursor.getLong(1), cursor.getLong(2));
            Log.d(TAG, "Setting alarm for: \n\n" + "\tRecordingId: " + cursor.getLong(0));
            cursor.moveToNext();
        }
    }
}
