package com.cpdev;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.cpdev.recording.RecordingBroadcastReceiver;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class AlarmHelper {
    private static final String TAG = "AlarmHelper";


    public void setAlarm(Context context, long stationId, long typeId, long startDateTime, long endDateTime) {

        DatabaseHelper databaseHelper = prepareDatabaseHelper(context);
        RadioDetails radioDetails = databaseHelper.getRadioDetail(stationId);
        databaseHelper.close();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RecordingBroadcastReceiver.class);
        intent.putExtra(context.getString(R.string.timed_recorder_service_name_key), radioDetails.getStationName());
        intent.putExtra(context.getString(R.string.timed_recorder_service_url_key), radioDetails.getPlaylistUrl());
        intent.putExtra(context.getString(R.string.timed_recorder_service_recording_duration), endDateTime - startDateTime);
        intent.putExtra(context.getString(R.string.timed_recorder_service_operation_key), typeId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Log.d(TAG, "startTime = " + new SimpleDateFormat("EEE dd/MM/yyyy HH:mm:ss").format(startDateTime));

        switch ((int) typeId) {

            case RadioApplication.ONE_OFF_SCHEDULED_RECORDING:
                alarmManager.set(AlarmManager.RTC_WAKEUP, startDateTime, pendingIntent);
                break;

            case RadioApplication.DAILY_SCHEDULED_RECORDING:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startDateTime, AlarmManager.INTERVAL_DAY, pendingIntent);
                break;

            case RadioApplication.WEEKLY_SCHEDULED_RECORDING:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startDateTime, AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                break;

        }


    }

    private DatabaseHelper prepareDatabaseHelper(Context context) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);

        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        return dbHelper;
    }
}
